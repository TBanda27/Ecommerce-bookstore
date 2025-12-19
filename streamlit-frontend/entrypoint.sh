#!/bin/sh
# Entrypoint script for Streamlit frontend

# Print debug info
echo "Current directory: $(pwd)"
echo "Files in current directory:"
ls -la
echo "Files in /app:"
ls -la /app

# Change to /app directory
cd /app

# Run Streamlit
exec streamlit run app.py --server.port=8501 --server.address=0.0.0.0 --server.headless=true
