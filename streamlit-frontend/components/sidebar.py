"""
Sidebar component with login/logout functionality
"""

import streamlit as st
from utils.auth import is_authenticated, logout, set_auth_data, is_admin
from utils.api_client import APIClient


def render_sidebar():
    """Render sidebar with authentication controls"""
    api = APIClient()

    st.sidebar.title("📚 Books Store")
    st.sidebar.divider()

    if not is_authenticated():
        # Login Form
        st.sidebar.subheader("Login")

        # Google OAuth2 Login Button
        oauth2_url = api.get_oauth2_login_url()
        st.sidebar.markdown(
            f'<a href="{oauth2_url}" target="_self" style="text-decoration: none;">'
            '<button style="width: 100%; padding: 0.5rem; background-color: #4285F4; color: white; '
            'border: none; border-radius: 0.25rem; cursor: pointer; font-size: 1rem; margin-bottom: 1rem;">'
            '🔐 Login with Google'
            '</button></a>',
            unsafe_allow_html=True
        )

        st.sidebar.caption("Or login with email/password:")

        with st.sidebar.form("login_form"):
            email = st.text_input("Email", key="login_email")
            password = st.text_input("Password", type="password", key="login_password")
            submit = st.form_submit_button("Login", use_container_width=True)

            if submit:
                if not email or not password:
                    st.error("Please enter email and password")
                else:
                    try:
                        result = api.login(email, password)
                        set_auth_data(result)
                        st.success(f"Welcome back, {result.get('username')}!")
                        st.rerun()
                    except Exception as e:
                        st.error(f"Login failed: {e}")

        st.sidebar.divider()
        st.sidebar.info("Don't have an account? Go to Register page")

    else:
        # User Info
        username = st.session_state.user
        st.sidebar.success(f"Logged in as **{username}**")

        if is_admin():
            st.sidebar.info("🛡️ Admin Account")

        # Logout Button
        if st.sidebar.button("Logout", use_container_width=True):
            logout()
            st.success("Logged out successfully!")
            st.rerun()

        st.sidebar.divider()

    # Navigation
    st.sidebar.subheader("Navigation")
    st.sidebar.page_link("app.py", label="📚 Browse Books")

    if is_authenticated():
        st.sidebar.page_link("pages/3_👤_Profile.py", label="👤 My Profile")
        st.sidebar.page_link("pages/4_⭐_My_Reviews.py", label="⭐ My Reviews")

    # Only show Admin Panel for users with ROLE_ADMIN
    if is_authenticated() and is_admin():
        st.sidebar.page_link("pages/_Admin_Panel.py", label="🛠️ Admin Panel")

    if not is_authenticated():
        st.sidebar.page_link("pages/2_📝_Register.py", label="📝 Register")

    st.sidebar.divider()
    st.sidebar.caption("Built by tbanda27 with Streamlit")
