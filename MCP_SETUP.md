# MCP Setup — Render / Vercel / Neon / MongoDB Atlas

Config lives in:

- Global: `C:\Users\ManishReddy\.cursor\mcp.json`
- Project: `.cursor/mcp.json`

## Current status (verified)

| Server | Status | Notes |
|--------|--------|--------|
| **neon** | Connected | Project `campusworks` found (`soft-term-29413905`) |
| **render** | Connected | Needs you to pick a workspace (see below) |
| **vercel** | Connected | Needs team id for project listing |
| **mongodb** | Needs reload | Windows `cmd /c npx` config updated — reload Cursor MCP |

## What you must do now

### 1. Reload Cursor
- Command Palette → **Developer: Reload Window**
- Or: **Cursor Settings → MCP** → refresh / restart servers

### 2. Confirm all four show green / Connected
Settings → **MCP** / **Tools & MCP**:
- `render`
- `vercel`
- `neon`
- `mongodb`

### 3. If MongoDB still errors
1. Ensure Atlas Network Access allows your IP (or `0.0.0.0/0`)
2. In MCP panel, click **Restart** on `mongodb`
3. Node **20+** is required (you have v22 — OK)

### 4. Tell me your Render workspace
After reload, say which workspace to use (I will list them). Example:
> “Use workspace My Workspace for Render”

### 5. Then ask me to debug deploys
Examples:
- “List Render services and failed deploy logs for cw-auth-service”
- “Check Vercel deployment for campus-works”
- “List Neon tables in campusworks_auth”
- “Count docs in campusworks_chat.messages”

## Security
- MongoDB MCP is **read-only**
- `.cursor/mcp.json` contains the Atlas URI — do not push to a public repo (add to `.gitignore` if needed)
