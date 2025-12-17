"""
User Profile Page
View and edit user profile
"""

import streamlit as st
from utils.auth import init_session_state, require_auth, logout
from utils.api_client import APIClient
from components.navbar import render_navbar
from components.footer import render_footer

# Page configuration
st.set_page_config(
    page_title="Books.to.scrape Clone - My Profile",
    page_icon="👤",
    layout="wide",
    initial_sidebar_state="collapsed"
)

# Initialize
init_session_state()
require_auth()
render_navbar()

st.title("👤 My Profile")

api = APIClient()

# Load current user data
try:
    user_data = api.get_current_user()

    st.subheader("Account Information")

    col1, col2 = st.columns(2)

    with col1:
        st.metric("Username", user_data.get('username', 'N/A'))

    with col2:
        st.metric("Email", user_data.get('email', 'N/A'))

    # Check roles
    roles = st.session_state.get('roles', [])
    if roles:
        st.write("**Roles**:", ", ".join(roles))

    # Verified status - check the 'enabled' field from backend
    is_verified = user_data.get('enabled', False)
    if is_verified:
        st.success("✓ Email Verified")
    else:
        st.warning("⚠️ Email Not Verified")
        st.info("Please check your email for the verification link.")

    st.divider()

    # Edit profile
    st.subheader("Edit Profile")

    with st.form("edit_profile_form"):
        new_username = st.text_input("Username", value=user_data.get('username', ''))
        st.caption("Email cannot be changed")

        col1, col2 = st.columns(2)

        with col1:
            update_submit = st.form_submit_button("Update Username", use_container_width=True)

        with col2:
            cancel = st.form_submit_button("Cancel", use_container_width=True)

        if update_submit:
            if not new_username:
                st.error("Username is required")
            else:
                try:
                    # Only update username, keep email the same
                    result = api.update_current_user(new_username, user_data.get('email'))
                    st.success("Username updated successfully!")
                    st.rerun()
                except Exception as e:
                    st.error(f"Error updating profile: {e}")

    st.divider()

    # Danger zone
    with st.expander("⚠️ Danger Zone"):
        st.warning("**Delete Account**: This action cannot be undone!")

        delete_confirm = st.text_input(
            "Type your username to confirm deletion",
            key="delete_confirm"
        )

        if st.button("Delete My Account", type="primary"):
            if delete_confirm == user_data.get('username'):
                try:
                    api.delete_current_user()
                    logout()
                    st.success("Account deleted successfully")
                    st.info("Redirecting to home...")
                    st.switch_page("app.py")
                except Exception as e:
                    st.error(f"Error deleting account: {e}")
            else:
                st.error("Username does not match. Deletion cancelled.")

except Exception as e:
    st.error(f"Error loading profile: {e}")
    st.info("Please try logging out and logging in again.")

# Footer
render_footer()
