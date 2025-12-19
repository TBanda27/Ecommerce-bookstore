#!/bin/sh
# Entrypoint script for Streamlit frontend

# Print debug info
echo "Current directory: $(pwd)"
echo "Files in current directory:"
ls -la
echo "Files in /app:"
ls -la /app
echo "Files in /app/pages:"
ls -la /app/pages/ || echo "pages directory empty or not found"
echo "Files in /app/components:"
ls -la /app/components/ || echo "components directory empty or not found"
echo "Searching for .py files:"
find /app -name "*.py" -type f || echo "No .py files found"

# Change to /app directory
cd /app

# Run Streamlit
exec streamlit run app.py --server.port=8501 --server.address=0.0.0.0 --server.headless=true
