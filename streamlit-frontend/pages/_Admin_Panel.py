"""
Admin Panel
Manage books, categories, inventory, prices, and users
"""

import streamlit as st
from utils.auth import init_session_state, require_admin
from utils.api_client import APIClient
from utils.helpers import format_currency, init_page_state
from utils.field_mapper import normalize_books_list, normalize_categories_list
from components.navbar import render_navbar
from components.footer import render_footer

# Page configuration
st.set_page_config(
    page_title="Books.to.scrape Clone - Admin Panel",
    page_icon="🛠️",
    layout="wide",
    initial_sidebar_state="collapsed"
)

# Initialize
init_session_state()

# Check admin access BEFORE rendering anything
require_admin()

render_navbar()

st.title("🛠️ Admin Panel")

api = APIClient()

# Tabs for different management sections
tab1, tab2, tab3, tab4, tab5 = st.tabs([
    "📚 Books",
    "📁 Categories",
    "💰 Prices",
    "📦 Inventory",
    "👥 Users"
])

# ==================== BOOKS TAB ====================
with tab1:
    st.header("Book Management")

    col1, col2 = st.columns([2, 1])

    with col2:
        if st.button("➕ Add New Book", use_container_width=True):
            st.session_state['adding_book'] = True

    # Add book form
    if st.session_state.get('adding_book', False):
        st.subheader("Add New Book")

        with st.form("add_book_form"):
            st.info("📘 Note: Book fields match the backend structure (name, uniqueProductCode, bookCoverImage)")

            name = st.text_input("Book Name*", help="The title/name of the book")
            description = st.text_area("Description*", help="Detailed description of the book")
            unique_product_code = st.text_input("Unique Product Code*", help="ISBN or unique identifier")
            book_cover_image = st.text_input("Book Cover Image URL*",
                                             value="https://via.placeholder.com/150",
                                             help="URL to book cover image")

            # Get categories for dropdown
            try:
                categories_data = api.get_categories(page=0, size=100)
                categories = normalize_categories_list(categories_data.get('content', []))
                category_dict = {cat['name']: cat['id'] for cat in categories}

                if categories:
                    selected_cat = st.selectbox("Category*", options=list(category_dict.keys()))
                    category_id = category_dict[selected_cat]
                else:
                    st.warning("No categories available. Please create a category first.")
                    category_id = None
            except Exception as e:
                st.error(f"Error loading categories: {e}")
                category_id = None

            st.divider()
            st.subheader("Price Information")
            col1, col2, col3 = st.columns(3)
            with col1:
                price_excl_vat = st.number_input("Price (excl VAT)*", min_value=0.01, value=19.99, step=0.01)
            with col2:
                tax_amount = st.number_input("Tax Amount*", min_value=0.0, value=3.99, step=0.01)
            with col3:
                currency = st.selectbox("Currency*", ["£", "$", "€"], index=0)

            st.divider()
            st.subheader("Inventory Information")
            col1, col2 = st.columns(2)
            with col1:
                stock_quantity = st.number_input("Stock Quantity*", min_value=0, value=100, step=1)
            with col2:
                availability_status = st.checkbox("Available for Sale", value=True)

            col1, col2 = st.columns(2)

            with col1:
                submit = st.form_submit_button("Create Book", use_container_width=True)

            with col2:
                cancel = st.form_submit_button("Cancel", use_container_width=True)

            if submit and category_id:
                if not all([name, description, unique_product_code, book_cover_image]):
                    st.error("Please fill in all required fields")
                else:
                    try:
                        result = api.create_book(
                            name, description, book_cover_image, unique_product_code,
                            category_id, price_excl_vat, tax_amount, currency,
                            stock_quantity, availability_status
                        )
                        st.success(f"Book '{name}' created successfully!")
                        st.session_state['adding_book'] = False
                        st.rerun()
                    except Exception as e:
                        st.error(f"Error creating book: {e}")

            if cancel:
                st.session_state['adding_book'] = False
                st.rerun()

    st.divider()

    # List books
    init_page_state("admin_books_page")

    try:
        current_page = st.session_state.get("admin_books_page", 0)
        books_data = api.get_books(page=current_page, size=10)
        books = normalize_books_list(books_data.get('content', []))

        if books:
            for book in books:
                book_title = book['title']
                if book.get('author'):
                    book_title += f" - {book['author']}"
                with st.expander(f"📖 {book_title}"):
                    col1, col2 = st.columns([2, 1])

                    with col1:
                        st.write(f"**ISBN**: {book.get('isbn', 'N/A')}")
                        st.write(f"**Category**: {book.get('categoryName', 'N/A')}")
                        if book.get('description'):
                            desc = book['description']
                            if len(desc) > 200:
                                desc = desc[:200] + "..."
                            st.write(f"**Description**: {desc}")

                    with col2:
                        # Price (from book data)
                        if book.get('priceIncVat'):
                            st.metric("Price", f"{book['currency']}{book['priceIncVat']:.2f}")

                        # Stock (from book data)
                        if book.get('stockStatus'):
                            st.metric("Stock", book['stockStatus'])

                        # Reviews
                        if book.get('numberOfReviews'):
                            st.metric("Reviews", book['numberOfReviews'])

                    # Actions
                    col1, col2 = st.columns(2)

                    with col1:
                        if st.button("✏️ Edit", key=f"edit_book_{book['id']}"):
                            st.session_state[f'editing_book_{book["id"]}'] = True
                            st.rerun()

                    with col2:
                        if st.button("🗑️ Delete", key=f"delete_book_{book['id']}"):
                            try:
                                api.delete_book(book['id'])
                                st.success("Book deleted!")
                                st.rerun()
                            except Exception as e:
                                st.error(f"Error: {e}")

                    # Edit form (shown when edit button clicked)
                    if st.session_state.get(f'editing_book_{book["id"]}', False):
                        st.divider()
                        st.subheader("Edit Book")

                        with st.form(f"edit_book_form_{book['id']}"):
                            st.info("📝 Updating book details (price and inventory edited separately)")

                            new_name = st.text_input("Book Name*", value=book.get('name', book.get('title', '')))
                            new_description = st.text_area("Description*", value=book.get('description', ''))
                            new_unique_product_code = st.text_input("Unique Product Code*",
                                                                    value=book.get('uniqueProductCode', book.get('isbn', '')))
                            new_book_cover_image = st.text_input("Book Cover Image URL*",
                                                                 value=book.get('bookCoverImage', 'https://via.placeholder.com/150'))

                            # Categories dropdown
                            try:
                                categories_data = api.get_categories(page=0, size=100)
                                categories = normalize_categories_list(categories_data.get('content', []))
                                category_dict = {cat['name']: cat['id'] for cat in categories}

                                current_cat = book.get('categoryName', '')
                                cat_index = list(category_dict.keys()).index(current_cat) if current_cat in category_dict else 0

                                selected_cat = st.selectbox("Category*", options=list(category_dict.keys()), index=cat_index)
                                new_category_id = category_dict[selected_cat]
                            except Exception as e:
                                st.error(f"Error loading categories: {e}")
                                new_category_id = book.get('categoryId', 0)

                            col1, col2 = st.columns(2)
                            with col1:
                                submit = st.form_submit_button("Save Changes", use_container_width=True)
                            with col2:
                                cancel = st.form_submit_button("Cancel", use_container_width=True)

                            if submit:
                                try:
                                    api.update_book(
                                        book['id'], new_name, new_description,
                                        new_book_cover_image, new_unique_product_code,
                                        new_category_id
                                    )
                                    st.success("Book updated successfully!")
                                    del st.session_state[f'editing_book_{book["id"]}']
                                    st.rerun()
                                except Exception as e:
                                    st.error(f"Error updating book: {e}")

                            if cancel:
                                del st.session_state[f'editing_book_{book["id"]}']
                                st.rerun()

            # Pagination
            total_pages = books_data.get('totalPages', 1)
            if total_pages > 1:
                col1, col2, col3 = st.columns([1, 2, 1])
                with col1:
                    if current_page > 0 and st.button("← Previous", key="books_prev"):
                        st.session_state["admin_books_page"] = current_page - 1
                        st.rerun()
                with col2:
                    st.write(f"Page {current_page + 1} of {total_pages}")
                with col3:
                    if current_page < total_pages - 1 and st.button("Next →", key="books_next"):
                        st.session_state["admin_books_page"] = current_page + 1
                        st.rerun()

    except Exception as e:
        st.error(f"Error loading books: {e}")

# ==================== CATEGORIES TAB ====================
with tab2:
    st.header("Category Management")

    col1, col2 = st.columns([2, 1])

    with col2:
        if st.button("➕ Add New Category", use_container_width=True):
            st.session_state['adding_category'] = True

    # Add category form
    if st.session_state.get('adding_category', False):
        st.subheader("Add New Category")

        with st.form("add_category_form"):
            category_name = st.text_input("Category Name*", help="2-255 characters")

            col1, col2 = st.columns(2)

            with col1:
                submit = st.form_submit_button("Create Category", use_container_width=True)

            with col2:
                cancel = st.form_submit_button("Cancel", use_container_width=True)

            if submit:
                if not category_name or len(category_name) < 2:
                    st.error("Category name is required (minimum 2 characters)")
                else:
                    try:
                        api.create_category(category_name)
                        st.success(f"Category '{category_name}' created!")
                        st.session_state['adding_category'] = False
                        st.rerun()
                    except Exception as e:
                        st.error(f"Error: {e}")

            if cancel:
                st.session_state['adding_category'] = False
                st.rerun()

    st.divider()

    # List categories
    try:
        categories_data = api.get_categories(page=0, size=50)
        categories = normalize_categories_list(categories_data.get('content', []))

        if categories:
            for cat in categories:
                with st.container(border=True):
                    col1, col2, col3 = st.columns([3, 1, 1])

                    with col1:
                        st.subheader(cat['name'])

                    with col2:
                        if st.button("✏️ Edit", key=f"edit_cat_{cat['id']}"):
                            st.session_state[f'editing_cat_{cat["id"]}'] = True
                            st.rerun()

                    with col3:
                        if st.button("🗑️ Delete", key=f"delete_cat_{cat['id']}"):
                            try:
                                api.delete_category(cat['id'])
                                st.success("Category deleted!")
                                st.rerun()
                            except Exception as e:
                                st.error(f"Error: {e}")

                    # Edit form
                    if st.session_state.get(f'editing_cat_{cat["id"]}', False):
                        st.divider()
                        with st.form(f"edit_cat_form_{cat['id']}"):
                            new_category_name = st.text_input("Category Name*", value=cat['name'], help="2-255 characters")

                            col1, col2 = st.columns(2)
                            with col1:
                                submit = st.form_submit_button("Save Changes", use_container_width=True)
                            with col2:
                                cancel = st.form_submit_button("Cancel", use_container_width=True)

                            if submit:
                                if not new_category_name or len(new_category_name) < 2:
                                    st.error("Category name is required (minimum 2 characters)")
                                else:
                                    try:
                                        api.update_category(cat['id'], new_category_name)
                                        st.success("Category updated!")
                                        del st.session_state[f'editing_cat_{cat["id"]}']
                                        st.rerun()
                                    except Exception as e:
                                        st.error(f"Error: {e}")

                            if cancel:
                                del st.session_state[f'editing_cat_{cat["id"]}']
                                st.rerun()
        else:
            st.info("No categories yet")

    except Exception as e:
        st.error(f"Error loading categories: {e}")

# ==================== PRICES TAB ====================
with tab3:
    st.header("Price Management")

    col1, col2 = st.columns([2, 1])

    with col2:
        if st.button("➕ Add New Price", use_container_width=True):
            st.session_state['adding_price'] = True

    # Add price form
    if st.session_state.get('adding_price', False):
        st.subheader("Add New Price")

        with st.form("add_price_form"):
            # Book selector
            try:
                books_data = api.get_books(page=0, size=100)
                books = normalize_books_list(books_data.get('content', []))
                book_dict = {book['title']: book['id'] for book in books}

                if books:
                    selected_book = st.selectbox("Select Book*", options=list(book_dict.keys()))
                    book_id = book_dict[selected_book]
                else:
                    st.warning("No books available")
                    book_id = None
            except Exception as e:
                st.error(f"Error loading books: {e}")
                book_id = None

            col1, col2, col3 = st.columns(3)
            with col1:
                price_excl_vat = st.number_input("Price (excl VAT)*", min_value=0.01, value=19.99, step=0.01)
            with col2:
                tax_amount = st.number_input("Tax Amount*", min_value=0.0, value=3.99, step=0.01)
            with col3:
                price_incl_vat = price_excl_vat + tax_amount
                st.metric("Price (incl VAT)", f"{price_incl_vat:.2f}")

            currency = st.selectbox("Currency*", ["£", "$", "€", "USD", "EUR", "GBP"])

            col1, col2 = st.columns(2)

            with col1:
                submit = st.form_submit_button("Create Price", use_container_width=True)

            with col2:
                cancel = st.form_submit_button("Cancel", use_container_width=True)

            if submit and book_id:
                try:
                    api.create_price(book_id, price_excl_vat, tax_amount, currency)
                    st.success("Price created!")
                    st.session_state['adding_price'] = False
                    st.rerun()
                except Exception as e:
                    st.error(f"Error: {e}")

            if cancel:
                st.session_state['adding_price'] = False
                st.rerun()

    st.divider()

    # List prices (through books)
    try:
        books_data = api.get_books(page=0, size=20)
        books = normalize_books_list(books_data.get('content', []))

        if books:
            st.subheader("Current Prices")

            for book in books:
                if book.get('priceIncVat') and book.get('priceId'):
                    with st.container(border=True):
                        col1, col2, col3, col4 = st.columns([3, 1, 1, 1])

                        with col1:
                            st.write(f"**{book['title']}**")
                            if book.get('categoryName'):
                                st.caption(book['categoryName'])

                        with col2:
                            st.metric("Price", f"{book['currency']}{book['priceIncVat']:.2f}")

                        with col3:
                            if st.button("✏️ Edit", key=f"edit_price_{book['priceId']}"):
                                # Calculate priceExclVat and taxAmount from priceIncVat
                                price_incl = book['priceIncVat']
                                # Estimate: assume 20% tax rate if not available
                                price_excl = book.get('priceExclVat', price_incl / 1.2)
                                tax = book.get('taxAmount', price_incl - price_excl)

                                st.session_state[f'editing_price_{book["priceId"]}'] = {
                                    'id': book['priceId'],
                                    'bookId': book['id'],
                                    'title': book['title'],
                                    'priceExclVat': price_excl,
                                    'taxAmount': tax,
                                    'currency': book.get('currency', '£')
                                }
                                st.rerun()

                        with col4:
                            st.caption("ID: " + str(book['priceId']))

                    # Edit form
                    if st.session_state.get(f'editing_price_{book["priceId"]}'):
                        price_data = st.session_state[f'editing_price_{book["priceId"]}']
                        st.divider()
                        st.subheader(f"Edit Price for: {price_data['title']}")

                        with st.form(f"edit_price_form_{book['priceId']}"):
                            col1, col2, col3 = st.columns(3)
                            with col1:
                                new_price_excl_vat = st.number_input("Price (excl VAT)*",
                                                                    min_value=0.01,
                                                                    value=float(price_data['priceExclVat']),
                                                                    step=0.01)
                            with col2:
                                new_tax_amount = st.number_input("Tax Amount*",
                                                                min_value=0.0,
                                                                value=float(price_data['taxAmount']),
                                                                step=0.01)
                            with col3:
                                new_price_incl = new_price_excl_vat + new_tax_amount
                                st.metric("Price (incl VAT)", f"{new_price_incl:.2f}")

                            new_currency = st.selectbox("Currency*", ["£", "$", "€", "USD", "EUR", "GBP"],
                                                       index=["£", "$", "€", "USD", "EUR", "GBP"].index(price_data['currency'])
                                                       if price_data['currency'] in ["£", "$", "€", "USD", "EUR", "GBP"] else 0)

                            col1, col2 = st.columns(2)
                            with col1:
                                submit = st.form_submit_button("Save Changes", use_container_width=True)
                            with col2:
                                cancel = st.form_submit_button("Cancel", use_container_width=True)

                            if submit:
                                try:
                                    api.update_price(price_data['id'], price_data['bookId'],
                                                   new_price_excl_vat, new_tax_amount, new_currency)
                                    st.success("Price updated successfully!")
                                    del st.session_state[f'editing_price_{book["priceId"]}']
                                    st.rerun()
                                except Exception as e:
                                    st.error(f"Error updating price: {e}")

                            if cancel:
                                del st.session_state[f'editing_price_{book["priceId"]}']
                                st.rerun()

    except Exception as e:
        st.error(f"Error loading prices: {e}")

# ==================== INVENTORY TAB ====================
with tab4:
    st.header("Inventory Management")

    col1, col2 = st.columns([2, 1])

    with col2:
        if st.button("➕ Add Inventory", use_container_width=True):
            st.session_state['adding_inventory'] = True

    # Add inventory form
    if st.session_state.get('adding_inventory', False):
        st.subheader("Add New Inventory")
        st.info("Note: Your backend has stock embedded in book data. This may not work.")

        with st.form("add_inventory_form"):
            # Book selector
            try:
                books_data = api.get_books(page=0, size=100)
                books = normalize_books_list(books_data.get('content', []))
                book_dict = {book['title']: book['id'] for book in books}

                if books:
                    selected_book = st.selectbox("Select Book*", options=list(book_dict.keys()), key="inv_book")
                    book_id = book_dict[selected_book]
                else:
                    st.warning("No books available")
                    book_id = None
            except Exception as e:
                st.error(f"Error loading books: {e}")
                book_id = None

            quantity = st.number_input("Stock Quantity*", min_value=0, value=100, step=1)
            available = st.checkbox("Available for Sale", value=True)

            col1, col2 = st.columns(2)

            with col1:
                submit = st.form_submit_button("Create Inventory", use_container_width=True)

            with col2:
                cancel = st.form_submit_button("Cancel", use_container_width=True)

            if submit and book_id:
                try:
                    # Backend uses stockQuantity and availabilityStatus
                    api.create_inventory(book_id, quantity, "")  # location not used
                    st.success("Inventory created!")
                    st.session_state['adding_inventory'] = False
                    st.rerun()
                except Exception as e:
                    st.error(f"Error: {e}")
                    st.info("Note: Backend may not support creating inventory (using read-only data)")

            if cancel:
                st.session_state['adding_inventory'] = False
                st.rerun()

    st.divider()

    # List inventory
    try:
        inventory_data = api.get_all_inventory(page=0, size=50)
        inventories = inventory_data.get('content', [])

        if inventories:
            st.subheader("Current Inventory")

            for inv in inventories:
                try:
                    from utils.field_mapper import normalize_book
                    book = normalize_book(api.get_book_by_id(inv['bookId']))

                    with st.container(border=True):
                        col1, col2, col3, col4, col5 = st.columns([3, 1, 1, 1, 1])

                        with col1:
                            st.write(f"**{book['title']}**")
                            if book.get('categoryName'):
                                st.caption(f"📁 {book['categoryName']}")

                        with col2:
                            st.metric("Quantity", inv.get('stockQuantity', inv.get('quantity', 0)))

                        with col3:
                            available = "✓ Available" if inv.get('availabilityStatus', False) else "✗ Unavailable"
                            st.write(f"**Status**: {available}")

                        with col4:
                            if st.button("✏️", key=f"edit_inv_{inv['id']}"):
                                st.session_state[f'editing_inv_{inv["id"]}'] = {
                                    'id': inv['id'],
                                    'title': book['title'],
                                    'quantity': inv.get('stockQuantity', inv.get('quantity', 0)),
                                    'available': inv.get('availabilityStatus', False)
                                }
                                st.rerun()

                        with col5:
                            if st.button("🗑️", key=f"delete_inv_{inv['id']}"):
                                try:
                                    api.delete_inventory_by_book_id(inv['bookId'])
                                    st.success("Inventory deleted!")
                                    st.rerun()
                                except Exception as e:
                                    st.error(f"Error: {e}")

                    # Edit form
                    if st.session_state.get(f'editing_inv_{inv["id"]}'):
                        inv_data = st.session_state[f'editing_inv_{inv["id"]}']
                        st.divider()
                        st.subheader(f"Edit Inventory for: {inv_data['title']}")

                        with st.form(f"edit_inv_form_{inv['id']}"):
                            new_quantity = st.number_input("Stock Quantity*", min_value=0, value=int(inv_data['quantity']), step=1)
                            new_availability = st.checkbox("Available for Sale", value=inv_data.get('available', new_quantity > 0))

                            col1, col2 = st.columns(2)
                            with col1:
                                submit = st.form_submit_button("Save Changes", use_container_width=True)
                            with col2:
                                cancel = st.form_submit_button("Cancel", use_container_width=True)

                            if submit:
                                try:
                                    api.update_inventory(inv_data['id'], inv['bookId'], new_quantity, new_availability)
                                    st.success("Inventory updated successfully!")
                                    del st.session_state[f'editing_inv_{inv["id"]}']
                                    st.rerun()
                                except Exception as e:
                                    st.error(f"Error updating inventory: {e}")

                            if cancel:
                                del st.session_state[f'editing_inv_{inv["id"]}']
                                st.rerun()
                except Exception as e:
                    st.error(f"Error loading book for inventory: {e}")

        else:
            st.info("No inventory records")

    except Exception as e:
        st.error(f"Error loading inventory: {e}")

# ==================== USERS TAB ====================
with tab5:
    st.header("User Management")

    init_page_state("admin_users_page")

    try:
        current_page = st.session_state.get("admin_users_page", 0)
        users_data = api.get_all_users(page=current_page, size=10)
        users = users_data.get('content', [])

        if users:
            st.caption(f"Total users: {users_data.get('totalElements', 0)}")

            for user in users:
                with st.container(border=True):
                    col1, col2, col3 = st.columns([2, 2, 1])

                    with col1:
                        st.write(f"**{user.get('username', 'N/A')}**")
                        st.caption(user.get('email', 'N/A'))

                    with col2:
                        roles = user.get('role', [])
                        if roles:
                            st.write("**Roles**: " + ", ".join(roles))

                        enabled = user.get('enabled', False)
                        if enabled:
                            st.success("✓ Verified")
                        else:
                            st.warning("⚠️ Not Verified")

                    with col3:
                        # Don't allow deleting yourself
                        if user['id'] != st.session_state.get('user_id'):
                            if st.button("🗑️ Delete", key=f"delete_user_{user['id']}"):
                                try:
                                    api.delete_user_by_id(user['id'])
                                    st.success("User deleted!")
                                    st.rerun()
                                except Exception as e:
                                    st.error(f"Error: {e}")
                        else:
                            st.info("You (cannot delete)")

            # Pagination
            total_pages = users_data.get('totalPages', 1)
            if total_pages > 1:
                col1, col2, col3 = st.columns([1, 2, 1])
                with col1:
                    if current_page > 0 and st.button("← Previous", key="users_prev"):
                        st.session_state["admin_users_page"] = current_page - 1
                        st.rerun()
                with col2:
                    st.write(f"Page {current_page + 1} of {total_pages}")
                with col3:
                    if current_page < total_pages - 1 and st.button("Next →", key="users_next"):
                        st.session_state["admin_users_page"] = current_page + 1
                        st.rerun()

    except Exception as e:
        st.error(f"Error loading users: {e}")

# Footer
render_footer()
