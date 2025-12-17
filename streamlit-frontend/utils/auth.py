"""
Authentication utilities for Streamlit frontend
Handles JWT tokens, session management, and role checking
"""

import streamlit as st
from typing import Optional, List
import base64
import json


def init_session_state():
    """Initialize session state variables"""
    if 'token' not in st.session_state:
        st.session_state.token = None
    if 'user' not in st.session_state:
        st.session_state.user = None
    if 'roles' not in st.session_state:
        st.session_state.roles = []
    if 'user_id' not in st.session_state:
        st.session_state.user_id = None


def is_authenticated() -> bool:
    """Check if user is authenticated"""
    return st.session_state.get('token') is not None


def is_admin() -> bool:
    """Check if current user has admin role"""
    roles = st.session_state.get('roles', [])
    return 'ROLE_ADMIN' in roles


def decode_jwt_payload(token: str) -> Optional[dict]:
    """Decode JWT token payload (without verification - just for display)"""
    try:
        # JWT format: header.payload.signature
        parts = token.split('.')
        if len(parts) != 3:
            return None

        # Decode payload (add padding if needed)
        payload = parts[1]
        padding = 4 - len(payload) % 4
        if padding != 4:
            payload += '=' * padding

        decoded = base64.urlsafe_b64decode(payload)
        return json.loads(decoded)
    except Exception as e:
        st.error(f"Error decoding token: {e}")
        return None


def set_auth_data(login_response: dict):
    """Set authentication data in session state from login response"""
    st.session_state.token = login_response.get('token')
    st.session_state.user = login_response.get('username')

    # Decode token to get roles and user_id
    if st.session_state.token:
        payload = decode_jwt_payload(st.session_state.token)
        if payload:
            st.session_state.roles = payload.get('roles', [])
            st.session_state.user_id = payload.get('userId')


def logout():
    """Clear authentication data"""
    st.session_state.token = None
    st.session_state.user = None
    st.session_state.roles = []
    st.session_state.user_id = None


def require_auth(redirect_message: str = "Please login to access this page"):
    """Decorator/function to require authentication for a page"""
    if not is_authenticated():
        st.warning(redirect_message)
        st.info("Use the sidebar to login or register")
        st.stop()


def require_admin(redirect_message: str = "Admin access required"):
    """Decorator/function to require admin role for a page"""
    require_auth()
    if not is_admin():
        st.error(redirect_message)
        st.stop()


def get_current_username() -> Optional[str]:
    """Get current logged-in username"""
    return st.session_state.get('user')


def get_current_user_id() -> Optional[int]:
    """Get current logged-in user ID"""
    return st.session_state.get('user_id')
