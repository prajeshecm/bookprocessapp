# Job Search & Apply Agent — 3 Implementation Paths, End to End

Three genuinely different ways to build the same thing:

| | **1. Raw Python + LLM** | **2. Claude Code** | **3. LangChain/LangGraph** |
|---|---|---|---|
| Who builds the agent loop | You, by hand | Claude Code (built-in) | LangGraph (`StateGraph`) |
| Framework dependency | None — just the `anthropic` SDK | Claude Code CLI + MCP | `langchain` + `langgraph` |
| Where it runs | Your own script/service | Your terminal, interactively | Your own script/service |
| Deployable as a hosted product | Yes | No | Yes |
| Best for | Understanding exactly what an "agent" is under the hood | Fast personal automation | The real capstone (mirrors GreenPlate) |

All three build the **same 7-stage pipeline**: parse resume → search jobs → score fit → tailor resume per job → **human approval** → apply → notify by email. What differs is who manages state and the loop.

---

# OPTION 1 — Raw Python + LLM (no LangChain, no LangGraph)

### Concept

An "agent" is just: **a loop that calls the LLM, checks if it asked for a tool, runs the tool, feeds the result back, repeats until done.** LangGraph gives you this loop pre-built with state persistence; here, you write it yourself in ~25 lines. Doing this once is genuinely useful — it demystifies what every agent framework is doing internally.

### Hello World

```bash
mkdir hello-raw && cd hello-raw
python -m venv venv && source venv/bin/activate
pip install anthropic python-dotenv
```

`main.py`:
```python
import anthropic
from dotenv import load_dotenv
load_dotenv()

client = anthropic.Anthropic()

def say_hello(name: str) -> str:
    return f"Hello, {name}! Raw-Python agent is alive."

TOOLS = [{
    "name": "say_hello",
    "description": "Greet a person by name.",
    "input_schema": {
        "type": "object",
        "properties": {"name": {"type": "string"}},
        "required": ["name"],
    },
}]
TOOL_FUNCS = {"say_hello": say_hello}

def run_agent(system_prompt: str, user_message: str, tools=TOOLS, tool_funcs=TOOL_FUNCS):
    messages = [{"role": "user", "content": user_message}]
    while True:
        response = client.messages.create(
            model="claude-sonnet-4-6",
            max_tokens=1024,
            system=system_prompt,
            tools=tools,
            messages=messages,
        )
        if response.stop_reason != "tool_use":
            return "".join(b.text for b in response.content if b.type == "text")

        messages.append({"role": "assistant", "content": response.content})
        tool_results = []
        for block in response.content:
            if block.type == "tool_use":
                result = tool_funcs[block.name](**block.input)
                tool_results.append({
                    "type": "tool_result",
                    "tool_use_id": block.id,
                    "content": str(result),
                })
        messages.append({"role": "user", "content": tool_results})

if __name__ == "__main__":
    print(run_agent("You are a friendly test agent.", "Greet Prajesh"))
```

Run it. If the greeting clearly came from `say_hello` (not the model just guessing), the loop works. This exact `run_agent()` function is what you'll reuse for every stage below.

### Full Job Search & Apply Agent

**Design choice:** instead of one giant agent trying to do all 7 stages in one conversation (fragile, hard to debug), run **one focused `run_agent()` call per stage**, passing clean data between them. This is the same principle LangGraph nodes follow — you're just wiring it by hand.

```
pipeline.py
├── stage_parse_resume(pdf_text) -> dict (candidate profile)
├── stage_search_jobs(preferences) -> list[dict] (raw postings)
├── stage_score_jobs(profile, postings) -> list[dict] (+ fit_score, reasoning)
├── stage_tailor_resumes(profile, scored_jobs) -> dict[company -> resume_path]
├── stage_approval(scored_jobs) -> list[str] (approved job ids)   ← plain input()
├── stage_apply(approved_jobs) -> list[dict] (application results)
└── stage_notify(results) -> None (sends email)
```

**1) Parse resume** — ask for strict JSON, validate manually:
```python
import json

def stage_parse_resume(resume_text: str) -> dict:
    prompt = f"""Extract this resume into JSON with keys:
    name, skills (list), years_experience (int), titles (list), summary (string).
    Return ONLY valid JSON, no markdown fences.

    Resume:
    {resume_text}"""
    raw = run_agent("You output only valid JSON.", prompt, tools=[], tool_funcs={})
    return json.loads(raw)
```

**2) Search jobs** — plain function tools wrapping real APIs (see §5 in the earlier guide re: which sources are safe to automate — Adzuna/JSearch/Greenhouse over LinkedIn/Glassdoor scraping):
```python
import requests

def search_adzuna(location: str, tech: str) -> list[dict]:
    r = requests.get("https://api.adzuna.com/v1/api/jobs/us/search/1", params={
        "app_id": ADZUNA_ID, "app_key": ADZUNA_KEY,
        "where": location, "what": tech, "results_per_page": 20,
    })
    return r.json()["results"]
```
No LLM call needed here at all — it's a plain function. Only wrap it as an LLM "tool" if you want the model deciding *which* sources/queries to run; otherwise just call it directly from your pipeline.

**3) Score jobs** — one `run_agent()` call per job, asking for a numeric score + reasoning (this is what makes "Walmart 95%, Google 99%" trustworthy rather than arbitrary):
```python
def stage_score_jobs(profile: dict, postings: list[dict]) -> list[dict]:
    scored = []
    for job in postings:
        prompt = f"""Candidate profile: {json.dumps(profile)}
        Job posting: {json.dumps(job)}
        Return ONLY JSON: {{"fit_score": <0-100 int>, "reasoning": "<2 sentences>"}}"""
        raw = run_agent("You are a precise job-fit evaluator. Output only JSON.", prompt, tools=[], tool_funcs={})
        result = json.loads(raw)
        scored.append({**job, **result})
    return sorted(scored, key=lambda j: j["fit_score"], reverse=True)
```

**4) Tailor resume per job** — same pattern, output is resume text you then render (use the `docx`/`pdf` skill logic, or a simple template):
```python
def stage_tailor_resumes(profile: dict, scored_jobs: list[dict], threshold=80) -> dict:
    resumes = {}
    for job in scored_jobs:
        if job["fit_score"] < threshold:
            continue
        prompt = f"""Base profile: {json.dumps(profile)}
        Target job: {json.dumps(job)}
        Rewrite the resume summary and reorder skills to emphasize fit for this job.
        Never invent experience the candidate doesn't have. Output plain text resume."""
        tailored = run_agent("You are a resume writer. Be honest — never fabricate.", prompt, tools=[], tool_funcs={})
        path = f"{job['company'].lower().replace(' ', '_')}_resume.pdf"
        render_to_pdf(tailored, path)   # your own PDF renderer
        resumes[job["id"]] = path
    return resumes
```

**5) Approval — the important gate.** No framework needed for this at all; it's just a checkpoint in your control flow:
```python
def stage_approval(scored_jobs: list[dict], resumes: dict) -> list[str]:
    print("\n--- JOBS FOR REVIEW ---")
    for job in scored_jobs:
        if job["id"] in resumes:
            print(f"{job['company']}: {job['fit_score']}% — {resumes[job['id']]}")
    ids = input("\nEnter approved job IDs, comma-separated: ")
    return [i.strip() for i in ids.split(",") if i.strip()]
```
This is exactly what LangGraph's `interrupt()` does under the hood, just without the ability to pause/resume across process restarts — a real limitation worth knowing (see Option 3).

**6) Apply** — Playwright, gated strictly on the approved-ID list, running only against Greenhouse/Lever-style forms:
```python
from playwright.sync_api import sync_playwright

def stage_apply(approved_ids: list[str], scored_jobs: list[dict], resumes: dict) -> list[dict]:
    results = []
    with sync_playwright() as p:
        browser = p.chromium.launch()
        for job in scored_jobs:
            if job["id"] not in approved_ids:
                continue
            page = browser.new_page()
            page.goto(job["apply_url"])
            page.set_input_files("input[type=file]", resumes[job["id"]])
            page.click("button[type=submit]")
            results.append({"company": job["company"], "status": "applied"})
        browser.close()
    return results
```

**7) Notify:**
```python
import smtplib
from email.mime.text import MIMEText

def stage_notify(results: list[dict]):
    body = "\n".join(f"{r['company']}: {r['status']}" for r in results)
    msg = MIMEText(body)
    msg["Subject"] = "Job Applications Sent"
    msg["From"] = msg["To"] = YOUR_EMAIL
    with smtplib.SMTP_SSL("smtp.gmail.com", 465) as s:
        s.login(YOUR_EMAIL, APP_PASSWORD)
        s.send_message(msg)
```

**Orchestrator** — the whole "agent" is just this:
```python
def main():
    profile = stage_parse_resume(open("resume.txt").read())
    postings = search_adzuna(location="Raleigh, NC", tech="python")
    scored = stage_score_jobs(profile, postings)
    resumes = stage_tailor_resumes(profile, scored)
    approved = stage_approval(scored, resumes)
    results = stage_apply(approved, scored, resumes)
    stage_notify(results)

if __name__ == "__main__":
    main()
```

That's it — no framework, and you can see exactly where every LLM call happens and why. The trade-off: no persistence (if the script crashes mid-run, you start over), no built-in retry/streaming, and you own all the plumbing.

---

# OPTION 2 — Claude Code (Skills, Tools, Subagents)

### Concept

Claude Code *is* the agent loop and the CLI at once. You don't write orchestration code — you describe capabilities (Skills), give it real side-effect tools (MCP servers), and optionally split work across Subagents. Approval is native: Claude Code already asks before risky actions, you're just reinforcing that at the right point.

### Hello World

(As before — quick recap.) `.claude/skills/greeter/SKILL.md` describing when to greet, plus one MCP tool `say_hello`. Run `claude`, ask it to greet you, confirm it calls the tool rather than just replying in prose.

### Full Job Search & Apply Agent

**Project layout:**
```
job-agent/
├── CLAUDE.md                        # top-level instructions
├── .claude/
│   ├── skills/
│   │   ├── resume-parsing/SKILL.md
│   │   ├── job-scoring/SKILL.md
│   │   ├── resume-tailoring/SKILL.md
│   │   └── application-review/SKILL.md
│   └── agents/                      # subagents
│       ├── searcher.md
│       ├── scorer.md
│       ├── tailor.md
│       └── applier.md
└── mcp-servers/
    ├── job_search_server.py         # Adzuna/JSearch/Greenhouse wrappers
    ├── resume_render_server.py      # text -> PDF
    ├── apply_server.py              # Playwright, gated
    └── email_server.py              # SMTP
```

**`CLAUDE.md`** (the "constitution" for this project — Claude Code reads this automatically):
```markdown
# Job Search & Apply Agent

This project searches jobs matching the candidate's resume and preferences,
scores fit, tailors a resume per job, and applies only after explicit
human approval.

## Pipeline order
1. Parse resume (resume-parsing skill)
2. Search jobs via job-search MCP tools (never scrape LinkedIn/Glassdoor directly)
3. Score each job 0-100 with reasoning (job-scoring skill)
4. Tailor a resume per job scoring 80+ (resume-tailoring skill), save as
   {company}_resume.pdf
5. STOP and show the full job list with scores and resume paths.
   Wait for explicit user approval of specific job IDs before proceeding.
6. Only call the apply tool for approved job IDs.
7. Email a summary via the email tool once applications are sent.

## Hard rule
Never call the apply tool without an explicit "approved" instruction
from the user in this session, naming the specific jobs.
```

**Skill example — `job-scoring/SKILL.md`:**
```markdown
---
name: job-scoring
description: Use when scoring how well a job posting matches the candidate's resume/profile. Produces a 0-100 fit score with reasoning.
---

Score fit based on: required skills overlap, years of experience match,
title/seniority alignment, and location/remote compatibility. Always give
2-3 sentences of reasoning alongside the number — a bare score isn't useful.
Be honest about weak matches; don't inflate scores.
```

**Subagent example — `agents/applier.md`:**
```markdown
---
name: applier
description: Submits job applications via the apply MCP tool. Only invoke for jobs the user has explicitly approved by ID.
tools: [apply_to_job, read_file]
---

You submit applications. You must receive an explicit list of approved
job IDs from the main conversation before calling apply_to_job. If no
approval list is present in context, stop and ask for one — do not proceed.
```

**MCP tool — `apply_server.py`** (same Playwright logic as Option 1, just exposed as an MCP tool instead of a plain function):
```python
from mcp.server.fastmcp import FastMCP
from playwright.sync_api import sync_playwright

mcp = FastMCP("apply-tools")

@mcp.tool()
def apply_to_job(job_url: str, resume_path: str) -> str:
    """Submit an application. Only call this for jobs the user has approved."""
    with sync_playwright() as p:
        browser = p.chromium.launch()
        page = browser.new_page()
        page.goto(job_url)
        page.set_input_files("input[type=file]", resume_path)
        page.click("button[type=submit]")
        browser.close()
    return f"Applied via {job_url}"
```

**Running it:** you'd literally type in the Claude Code session:
```
> Parse my resume at resume.pdf, search for python roles in Raleigh NC,
  score and tailor resumes for anything 80%+, then show me the list.
```
Claude Code runs stages 1–4, then — because `CLAUDE.md` tells it to — stops and shows you the list instead of continuing. You reply:
```
> Approve job_003 and job_007, reject the rest
```
Only then does it invoke the `applier` subagent.

**What you get for free vs. Option 1:** no orchestration code, no manual `run_agent()` loop, approval and reasoning-transparency come from good `CLAUDE.md`/`SKILL.md` writing rather than code. **What you give up:** it's a session you run by hand — there's no persistent server, no API you could point a frontend at, no scheduled/background execution without additional tooling.

---

# OPTION 3 — LangChain / LangGraph

### Concept

Same as your GreenPlate build: `StateGraph`, typed state, `context_schema`, middleware, and — new here — `interrupt()` for the approval gate. This is the only option with built-in **persistence across the approval pause** (via a checkpointer), which matters a lot for a multi-day job search where you might not approve jobs the same day they're found.

### Hello World

```bash
pip install langchain langgraph langchain-anthropic python-dotenv
```
```python
from langchain_anthropic import ChatAnthropic
from langgraph.prebuilt import create_agent

def say_hello(name: str) -> str:
    """Greet a person by name."""
    return f"Hello, {name}! LangGraph agent is alive."

agent = create_agent(model=ChatAnthropic(model="claude-sonnet-4-6"), tools=[say_hello])
result = agent.invoke({"messages": [{"role": "user", "content": "Greet Prajesh"}]})
print(result["messages"][-1].content)
```

### Full Job Search & Apply Agent

**State + context (mirrors `RestaurantContext` from GreenPlate):**
```python
from typing import TypedDict
from dataclasses import dataclass

class AgentState(TypedDict):
    profile: dict
    postings: list[dict]
    scored_jobs: list[dict]
    resumes: dict
    approved_ids: list[str]
    apply_results: list[dict]

@dataclass
class JobSearchContext:
    location: str
    tech_stack: list[str]
    min_salary: int | None = None
    remote_ok: bool = True
```

**Nodes** — each one is a focused function, same shape as GreenPlate's tool/middleware nodes:
```python
def parse_resume_node(state: AgentState, runtime) -> dict:
    profile = model.with_structured_output(CandidateProfile).invoke(
        f"Extract profile from: {state['resume_text']}"
    )
    return {"profile": profile.model_dump()}

def search_jobs_node(state: AgentState, runtime) -> dict:
    ctx: JobSearchContext = runtime.context
    postings = search_adzuna(ctx.location, ctx.tech_stack)
    return {"postings": postings}

def score_jobs_node(state: AgentState, runtime) -> dict:
    scored = [score_one(state["profile"], job) for job in state["postings"]]
    return {"scored_jobs": sorted(scored, key=lambda j: -j["fit_score"])}

def tailor_resumes_node(state: AgentState, runtime) -> dict:
    resumes = {j["id"]: tailor_one(state["profile"], j) for j in state["scored_jobs"] if j["fit_score"] >= 80}
    return {"resumes": resumes}
```

**The approval node — this is the one genuinely new LangGraph concept:**
```python
from langgraph.types import interrupt

def approval_node(state: AgentState, runtime) -> dict:
    decision = interrupt({
        "message": "Review and approve jobs to apply to",
        "jobs": state["scored_jobs"],
        "resumes": state["resumes"],
    })
    return {"approved_ids": decision["approved_ids"]}

def apply_node(state: AgentState, runtime) -> dict:
    results = []
    for job in state["scored_jobs"]:
        if job["id"] in state["approved_ids"]:
            results.append(apply_via_playwright(job, state["resumes"][job["id"]]))
    return {"apply_results": results}

def notify_node(state: AgentState, runtime) -> dict:
    send_email_summary(state["apply_results"])
    return {}
```

**Graph wiring:**
```python
from langgraph.graph import StateGraph, START, END
from langgraph.checkpoint.redis import AsyncRedisSaver   # same store family as GreenPlate

graph = StateGraph(AgentState, context_schema=JobSearchContext)
graph.add_node("parse_resume", parse_resume_node)
graph.add_node("search_jobs", search_jobs_node)
graph.add_node("score_jobs", score_jobs_node)
graph.add_node("tailor_resumes", tailor_resumes_node)
graph.add_node("approval", approval_node)
graph.add_node("apply", apply_node)
graph.add_node("notify", notify_node)

graph.add_edge(START, "parse_resume")
graph.add_edge("parse_resume", "search_jobs")
graph.add_edge("search_jobs", "score_jobs")
graph.add_edge("score_jobs", "tailor_resumes")
graph.add_edge("tailor_resumes", "approval")
graph.add_edge("approval", "apply")
graph.add_edge("apply", "notify")
graph.add_edge("notify", END)

app = graph.compile(checkpointer=AsyncRedisSaver(...))
```

**Running it — the pause/resume that only this option gives you for free:**
```python
config = {"configurable": {"thread_id": "job-search-2026-08"}}
context = JobSearchContext(location="Raleigh, NC", tech_stack=["python", "kafka"])

# Day 1: runs through to the approval interrupt, then pauses
result = await app.ainvoke({"resume_text": resume_text}, config=config, context=context)

# ... you close your laptop, come back two days later ...

# Day 3: resume exactly where it paused, checkpointed in Redis
from langgraph.types import Command
final = await app.ainvoke(
    Command(resume={"approved_ids": ["job_003", "job_007"]}),
    config=config,
)
```
That resumability — walking away for two days mid-approval without losing state — is the concrete thing LangGraph buys you over both other options, and it's exactly the same `AsyncRedisSaver`/`thread_id` pattern from GreenPlate.

**Dynamic tool gating**, same idea as your `@before_model`/`@wrap_tool_call` middleware in GreenPlate: wrap the apply tool so it literally cannot be invoked unless `state["approved_ids"]` is non-empty — belt-and-suspenders on top of the graph structure only routing there post-approval.

---

# Recommendation

- **Build Option 1 first, briefly** — even just the hello-world and the scoring stage — purely so the LangGraph version stops feeling like magic. You'll write better LangGraph code once you've felt the loop by hand.
- **Build Option 3 (LangGraph) as the real, deployable project** — it's the direct continuation of GreenPlate, it's the one that survives you closing your laptop mid-approval, and it's the strongest thing to put in front of an interviewer.
- **Keep Option 2 (Claude Code) in your back pocket** for the actual week-to-week job hunting — once the LangGraph version exists, you can even have Claude Code *call into it* as a tool, rather than duplicating the pipeline.
