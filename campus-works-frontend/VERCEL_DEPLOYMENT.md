# Vercel Deployment Guide for CampusWorks Frontend

## Issue Fixed
The 404 error was occurring because Vercel didn't know how to handle client-side routing in your React SPA. The `vercel.json` configuration file has been added to fix this.

## What was added:

### 1. vercel.json
- Configured proper SPA routing to serve `index.html` for all routes
- Added API proxy configuration for backend calls
- Added static asset handling for fonts, images, etc.
- Added security headers
- Added caching for static assets

### 2. Updated vite.config.js
- Added preview configuration for better Vercel compatibility

## Environment Variables Setup

In your Vercel dashboard, go to Settings > Environment Variables and add:

```
VITE_API_BASE_URL=https://your-backend-api-url.com
VITE_APP_NAME=CampusWorks
VITE_APP_VERSION=1.0.0
VITE_RAZORPAY_KEY_ID=your_razorpay_key_id
VITE_ENABLE_REDUX_DEVTOOLS=false
```

## Important Notes:

1. **Update Backend URL**: Replace `https://your-backend-api-url.com` in `vercel.json` with your actual backend API URL.

2. **Environment Variables**: Make sure to set the environment variables in Vercel dashboard, especially `VITE_API_BASE_URL`.

3. **Redeploy**: After making these changes, redeploy your application on Vercel.

## How it works:

- All routes (like `/login`, `/dashboard`, etc.) now serve the `index.html` file
- React Router handles the client-side routing
- API calls are proxied to your backend
- Static assets are served with proper caching headers

## Testing:

After deployment, test these URLs:
- `https://your-app.vercel.app/` (should show landing page)
- `https://your-app.vercel.app/login` (should show login page)
- `https://your-app.vercel.app/dashboard` (should show dashboard if authenticated)

The 404 error should now be resolved!
