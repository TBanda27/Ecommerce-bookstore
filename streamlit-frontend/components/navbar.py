"""
Navigation bar component - replaces sidebar
"""

import streamlit as st
from utils.auth import is_authenticated, logout, set_auth_data, is_admin
from utils.api_client import APIClient


def render_navbar():
    """Render top navigation bar"""

    # Custom CSS for navbar
    st.markdown("""
        <style>
        .navbar {
            background-color: #f8f9fa;
            padding: 1rem 2rem;
            border-bottom: 2px solid #e9ecef;
            margin-bottom: 2rem;
        }
        .navbar-title {
            font-size: 1.5rem;
            font-weight: bold;
            color: #FF4B4B;
            margin: 0;
        }
        .navbar-subtitle {
            font-size: 0.9rem;
            color: #666;
            margin: 0;
        }
        </style>
    """, unsafe_allow_html=True)

    # Title section
    st.markdown("""
        <div class="navbar">
            <div class="navbar-title">📚 Books.to.scrape Clone</div>
            <div class="navbar-subtitle">A bookstore with enhanced features</div>
        </div>
    """, unsafe_allow_html=True)

    # Navigation and auth in columns
    if is_authenticated():
        # Authenticated user navbar
        username = st.session_state.user

        col1, col2, col3, col4, col5, col6 = st.columns([2, 2, 2, 2, 2, 2])

        with col1:
            if st.button("📚 Browse Books", use_container_width=True):
                st.switch_page("app.py")

        with col2:
            if st.button("👤 My Profile", use_container_width=True):
                st.switch_page("pages/3_👤_Profile.py")

        with col3:
            if st.button("⭐ My Reviews", use_container_width=True):
                st.switch_page("pages/4_⭐_My_Reviews.py")

        with col4:
            if is_admin():
                if st.button("🛠️ Admin Panel", use_container_width=True):
                    st.switch_page("pages/_Admin_Panel.py")
            else:
                st.write("")  # Empty space

        with col5:
            st.write(f"**{username}**")
            if is_admin():
                st.caption("🛡️ Admin")

        with col6:
            if st.button("Logout", use_container_width=True, type="primary"):
                logout()
                st.success("Logged out!")
                st.rerun()

    else:
        # Guest user navbar
        col1, col2, col3, col4 = st.columns([3, 3, 3, 3])

        with col1:
            if st.button("📚 Browse Books", use_container_width=True):
                st.switch_page("app.py")

        with col2:
            if st.button("📝 Register", use_container_width=True):
                st.switch_page("pages/2_📝_Register.py")

        with col3:
            # Login modal trigger
            if st.button("🔐 Login", use_container_width=True, type="primary"):
                st.session_state['show_login_modal'] = True

        with col4:
            # Google OAuth button
            api = APIClient()
            oauth2_url = api.get_oauth2_login_url()
            st.markdown(
                f'<a href="{oauth2_url}" target="_self" style="text-decoration: none;">'
                '<button style="width: 100%; padding: 0.5rem; background-color: #4285F4; color: white; '
                'border: none; border-radius: 0.25rem; cursor: pointer; font-size: 1rem;">'
                'Login with Google'
                '</button></a>',
                unsafe_allow_html=True
            )

    # Login modal for email/password
    if not is_authenticated() and st.session_state.get('show_login_modal', False):
        st.divider()
        st.subheader("🔐 Login")

        api = APIClient()

        with st.form("login_form"):
            email = st.text_input("Email")
            password = st.text_input("Password", type="password")

            col1, col2 = st.columns(2)
            with col1:
                submit = st.form_submit_button("Login", use_container_width=True)
            with col2:
                cancel = st.form_submit_button("Cancel", use_container_width=True)

            if submit:
                if not email or not password:
                    st.error("Please enter email and password")
                else:
                    try:
                        result = api.login(email, password)
                        set_auth_data(result)
                        st.success(f"Welcome back, {result.get('username')}!")
                        st.session_state['show_login_modal'] = False
                        st.rerun()
                    except Exception as e:
                        st.error(f"Login failed: {e}")

            if cancel:
                st.session_state['show_login_modal'] = False
                st.rerun()

    st.divider()
