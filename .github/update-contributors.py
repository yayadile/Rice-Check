#!/usr/bin/env python3
"""Update the Contributors section in README.md from the GitHub API.

Reads the contributor list for this repo, builds a ranked table
(commits + share %) and a Mermaid pie chart, and replaces the block
between the markers in README.md:

    <!-- CONTRIBUTORS:START -->
    ... generated content ...
    <!-- CONTRIBUTORS:END -->

If the markers are missing, the section is appended to the README.
Runs in GitHub Actions with GITHUB_TOKEN, but also works locally
(unauthenticated, public repo only) for testing.
"""

import json
import os
import re
import sys
import urllib.request

START = "<!-- CONTRIBUTORS:START -->"
END = "<!-- CONTRIBUTORS:END -->"
README = "README.md"


def get_contributors(repo, token):
    url = f"https://api.github.com/repos/{repo}/contributors?per_page=100"
    req = urllib.request.Request(url, headers={"Accept": "application/vnd.github+json"})
    if token:
        req.add_header("Authorization", f"Bearer {token}")
    with urllib.request.urlopen(req, timeout=30) as resp:
        data = json.load(resp)
    # [{"login": "...", "contributions": N, ...}, ...] sorted desc by API
    contributors = [(c["login"], int(c["contributions"])) for c in data]
    contributors.sort(key=lambda x: -x[1])
    return contributors


def build_block(contributors):
    total = sum(n for _, n in contributors) or 1
    lines = [
        "## Contributors",
        "",
        "| Rank | Contributor | Commits | Share |",
        "| --- | --- | --- | --- |",
    ]
    for i, (login, n) in enumerate(contributors, 1):
        pct = round(n / total * 100)
        lines.append(f"| {i} | {login} | {n} | {pct}% |")
    lines += [
        "",
        f"Total: {total} contributions.",
        "",
        "```mermaid",
        "pie title Rice-Check Contributors",
    ]
    for login, n in contributors:
        lines.append(f'    "{login}" : {n}')
    lines.append("```")
    return "\n".join(lines)


def main():
    repo = os.environ.get("GITHUB_REPOSITORY", "yayadile/Rice-Check")
    token = os.environ.get("GITHUB_TOKEN", "")
    try:
        contributors = get_contributors(repo, token)
    except Exception as e:
        print(f"Failed to fetch contributors for {repo}: {e}", file=sys.stderr)
        return 1
    if not contributors:
        print("No contributors found, leaving README unchanged.")
        return 0

    block = build_block(contributors)
    with open(README, encoding="utf-8") as f:
        readme = f.read()

    pattern = re.compile(
        re.escape(START) + r".*?" + re.escape(END), re.DOTALL
    )
    replacement = f"{START}\n{block}\n{END}"
    if pattern.search(readme):
        new_readme = pattern.sub(replacement, readme, count=1)
    else:
        new_readme = readme.rstrip("\n") + f"\n\n{replacement}\n"

    if new_readme == readme:
        print("README already up to date.")
        return 0
    with open(README, "w", encoding="utf-8") as f:
        f.write(new_readme)
    print(f"Updated README contributors: {contributors}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
