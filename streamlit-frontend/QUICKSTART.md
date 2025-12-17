# Quick Start Guide

Get your Streamlit frontend running in 5 minutes!

## Prerequisites Check

Before starting, ensure:
- [ ] Python 3.11+ installed (`python --version`)
- [ ] Backend services are running (API Gateway on `http://localhost:9090`)
- [ ] You can access backend health check: `http://localhost:9090/actuator/health`

## Quick Setup

### 1. Navigate to the frontend directory
```bash
cd "D:\Spring Boot\Books-Ecommerce Project\streamlit-frontend"
```

### 2. Create virtual environment
```bash
python -m venv venv
```

### 3. Activate virtual environment

**Windows (Command Prompt)**:
```cmd
venv\Scripts\activate
```

**Windows (PowerShell)**:
```powershell
venv\Scripts\Activate.ps1
```

**Linux/Mac**:
```bash
source venv/bin/activate
```

### 4. Install dependencies
```bash
pip install -r requirements.txt
```

### 5. Create environment file
```bash
# Copy the example
copy .env.example .env   # Windows
# cp .env.example .env   # Linux/Mac

# Edit .env if needed (default should work)
```

### 6. Run the application
```bash
streamlit run app.py
```

### 7. Access the app
Open your browser and go to: **http://localhost:8501**

## What's Next?

### First Time Setup

1. **Register an Admin Account**:
   - Go to Register page
   - Create your first account
   - This will be your admin account

2. **Verify Email** (Optional for local development):
   - Check your email for verification link
   - Or skip verification for local testing

3. **Login**:
   - Use the sidebar login form
   - Enter your credentials

4. **Add Sample Data** (As Admin):
   - Go to Admin Panel
   - Create some categories (Fiction, Non-Fiction, etc.)
   - Add some books
   - Set prices and inventory

5. **Test User Features**:
   - Browse books
   - Write reviews
   - Update your profile

## Common Commands

### Start the app
```bash
streamlit run app.py
```

### Start with custom port
```bash
streamlit run app.py --server.port=8502
```

### Clear cache and restart
```bash
streamlit run app.py --server.runOnSave=true
```

### Update dependencies
```bash
pip install -r requirements.txt --upgrade
```

## Docker Quick Start

### Build and run with Docker
```bash
# Build
docker build -t books-streamlit .

# Run
docker run -p 8501:8501 -e API_GATEWAY_URL=http://host.docker.internal:9090 books-streamlit
```

### Or use Docker Compose
```bash
docker-compose up -d
```

## Troubleshooting

### Backend not reachable
```
Error: Error loading books: Connection refused
```
**Fix**: Start your Spring Boot services first

### Port already in use
```
Port 8501 is already in use
```
**Fix**: Use a different port:
```bash
streamlit run app.py --server.port=8502
```

### Module not found
```
ModuleNotFoundError: No module named 'streamlit'
```
**Fix**: Activate venv and install dependencies:
```bash
venv\Scripts\activate  # Windows
pip install -r requirements.txt
```

## Testing the Application

### Test as Regular User
1. Register a new account
2. Browse books
3. View book details
4. Write a review
5. Edit/delete your review
6. Update your profile

### Test as Admin
1. Login with admin account
2. Create categories
3. Add books
4. Set prices
5. Manage inventory
6. View all users

## Need Help?

- Read the full [README.md](README.md)
- Check [Streamlit Documentation](https://docs.streamlit.io)
- Review backend API at `http://localhost:9090/swagger-ui.html`
- Ensure all microservices are running (Eureka, API Gateway, Auth, Books, etc.)

## Next Steps

- [ ] Customize the theme in `.streamlit/config.toml`
- [ ] Add your own branding
- [ ] Deploy to production
- [ ] Set up monitoring
- [ ] Configure backups

Happy coding!
