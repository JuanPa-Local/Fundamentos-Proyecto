document.addEventListener('DOMContentLoaded', () => {
    const booksGrid = document.getElementById('booksGrid');
    const searchInput = document.getElementById('searchInput');
    const searchBtn = document.getElementById('searchBtn');

    // Cargar catálogo y categorías
    loadCategories();
    loadCatalog();

    // Event listeners
    if (searchBtn) {
        searchBtn.addEventListener('click', () => {
            loadCatalog(searchInput.value);
        });
    }

    if (searchInput) {
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') loadCatalog(searchInput.value);
        });
    }

    const categoryFilter = document.getElementById('categoryFilter');
    if (categoryFilter) {
        categoryFilter.addEventListener('change', () => {
            loadCatalog(searchInput ? searchInput.value : '');
        });
    }

    async function loadCategories() {
        if (!categoryFilter) return;
        try {
            const categories = await api.get('/books/categories');
            categories.forEach(c => {
                const opt = document.createElement('option');
                opt.value = c;
                opt.textContent = c;
                categoryFilter.appendChild(opt);
            });
        } catch (e) {
            console.error('Error cargando categorías', e);
        }
    }

    async function loadCatalog(query = '') {
        try {
            if (booksGrid) booksGrid.innerHTML = '<p>Cargando libros...</p>';
            
            let url = '/books';
            const cat = categoryFilter ? categoryFilter.value : '';
            
            if (query && cat) {
                url = `/books/catalog/filter?q=${encodeURIComponent(query)}&category=${encodeURIComponent(cat)}`;
            } else if (query) {
                url = `/books/catalog?q=${encodeURIComponent(query)}`;
            } else if (cat) {
                url = `/books/catalog/filter?category=${encodeURIComponent(cat)}`;
            }

            const books = await api.get(url);
            
            if (!books || books.length === 0) {
                if (booksGrid) booksGrid.innerHTML = '<p>No se encontraron libros.</p>';
                return;
            }

            renderBooks(books);
        } catch (error) {
            console.error(error);
            if (booksGrid) booksGrid.innerHTML = '<p class="error-message" style="display:block">Error al cargar el catálogo.</p>';
        }
    }

    function renderBooks(books) {
        if (!booksGrid) return;
        booksGrid.innerHTML = '';
        
        books.forEach(book => {
            // Solo mostrar libros aprobados (o todos si no hay restricción fuerte)
            if (book.status && book.status !== 'APROBADO') return;

            const card = document.createElement('div');
            card.className = 'book-card';
            
            const coverUrl = book.coverUrl || 'https://via.placeholder.com/220x300?text=Sin+Portada';
            
            card.innerHTML = `
                <div onclick="if(typeof openBookDetails === 'function') openBookDetails('${book.id}')" style="cursor:pointer;">
                    <img src="${coverUrl}" alt="Portada de ${book.title}" class="book-cover" onerror="this.src='https://via.placeholder.com/220x300?text=Sin+Portada'">
                    <div class="book-info">
                        <h3 class="book-title">${book.title}</h3>
                        <p class="book-author">${book.author}</p>
                        <p class="book-price">$${book.price.toFixed(2)}</p>
                    </div>
                </div>
                <div style="padding: 0 1.5rem 1.5rem;">
                    <button class="btn btn-outline mt-1" style="width:100%" onclick="addToCart('${book.id}')">Agregar al Carrito</button>
                </div>
            `;
            
            booksGrid.appendChild(card);
        });
    }
});

async function addToCart(bookId) {
    const user = api.auth.getCurrentUser();
    if (!user) {
        alert("Debes iniciar sesión para agregar al carrito.");
        window.location.href = 'login.html';
        return;
    }

    if (user.role !== 'BUYER') {
        alert("Solo los compradores pueden agregar al carrito.");
        return;
    }

    try {
        await api.post(`/cart/${user.id}/items/${bookId}`);
        alert("Libro agregado al carrito exitosamente!");
    } catch (error) {
        alert("Error al agregar al carrito: " + error.message);
    }
}
