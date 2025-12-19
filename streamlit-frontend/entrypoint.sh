#!/bin/sh
# Entrypoint script for Streamlit frontend

# Use Railway's PORT environment variable or default to 8501
PORT=${PORT:-8501}

echo "Starting Streamlit on port $PORT"

# Change to /app directory
cd /app

# Run Streamlit
exec streamlit run app.py --server.port=$PORT --server.address=0.0.0.0 --server.headless=true
