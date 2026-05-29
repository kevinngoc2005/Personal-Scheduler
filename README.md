# Scheduler

> A goal-oriented scheduling app that tracks not just when you work, but whether you're actually making progress.

**Status:** Phases 0–1 complete · prototype with full CRUD and calendar views shipped · analytics pipeline (Phase 2) in design

![Schedule monthly view](docs/images/preview.png)

---

Let's be honest: no engineer starts a project knowing exactly what they're doing. You figure it out piece by piece — trial, error, and a fair amount of rewriting — until the thing you pictured slowly becomes real. This document is the early blueprint for that process.

It's a planning doc, so it's rough on purpose. The goal isn't a polished spec; it's to lay out what I'm building, why, and how I'm thinking about it, so all that trial-and-error has a direction. I'm using this project to sharpen my skills in Java, databases, and data engineering, and you'll see the thinking evolve as it goes.

Spotted something I could do better? I'd genuinely love to hear it — open an issue, or reach me at the email below.

## Why it exists

Most calendar apps are great at telling you when things happen, but not whether you're making progress on what actually matters. This project aims to:

- Let people schedule daily, weekly, and monthly tasks in one place
- Break long-term goals down into the smaller work that gets them done
- Show, over time, how much of that work you're actually completing

This isn't just a calendar. It's built around *a goal hierarchy*: you break a goal into sub-goals, tasks, and sub-tasks, mark what you finish, and the app turns that activity into *self-analytics* — completion rates, streaks, and progress toward each goal's deadline. The scheduling is the input; the self-analytics is the payoff. That insights layer is the thing a standard calendar app doesn't give you.

## Features

- Monthly, weekly, and daily calendar views
- Goal hierarchy: Module → Sub-goal → Task → Sub-task
- Task completion tracking, with timestamps
- Insights dashboard: completion trends, streaks, goal progress *(planned)*
- Local data storage with export *(planned)*

## Tech stack

- Java (Swing), built in NetBeans
- SQLite, with a planned upgrade path to PostgreSQL
- JDBC for database connectivity
- Python *(planned, for the stats pipeline)*

## Architecture

The app is built in three parts:

![Pipeline architecture: app tables → stats script → stats tables → insights screen](docs/images/architecture.png)

1. **Capture (the app)** — the Swing interface reads and writes your goals, tasks, and completions to a database.
2. **Pipeline (the stats job)** — a separate script reads that live data on a schedule, computes summaries (completions per week, streaks, goal progress), and stores them in their own tables.
3. **Dashboard (the insights view)** — reads those summaries and shows how you're doing over time.

The architectural shape is classic ETL in miniature: a transactional source (live app tables), a scheduled job that snapshots and aggregates (the stats script), and a read-only consumer (the insights dashboard).

## Phase status

- **Phase 0 — done.** Working prototype: core create / edit / delete loop, one view.
- **Phase 1 — done.** Move storage to a real database (SQLite).
- **Phase 2 — up next.** Stats pipeline + insights screen. *This is the data-engineering core of the project.*
- **Phase 3 — partially shipped.** Polish, plus optional PostgreSQL, scheduling, and data export. *Already in: monthly / weekly / daily calendar views, full CRUD across the hierarchy with cascade deletes. Still planned: data export, optional PostgreSQL upgrade.*

## Why I'm building this

I'm using this project to sharpen three things I want to be strong at coming out of my junior year: Java, relational databases, and the data-engineering thinking that turns raw events into something actually useful. The personal hook is that I'd rather build my own scheduler than wedge my life into a calendar app that can't tell me whether I'm actually making progress on what matters. "Done," for me, looks like an analytics layer that surfaces my real completion patterns, and a codebase I'm confident defending end-to-end — including the pieces I prototyped with AI assistance.

## Design and data model

Detailed wireframes, screen flows, and the underlying entity design live in [DESIGN.pdf](DESIGN.pdf).

## Contact

Questions, criticism, and ideas are all welcome.

**Kevin Tran** · trankn2005@gmail.com
