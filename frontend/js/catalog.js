document.addEventListener('DOMContentLoaded', () => {
    const booksGrid = document.getElementById('booksGrid');
    const searchInput = document.getElementById('searchInput');
    const searchBtn = document.getElementById('searchBtn');
    const categoryFilter = document.getElementById('categoryFilter');

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
            const categories = book.categories && book.categories.length > 0 ? book.categories : [];
            const tagsHtml = categories.map(c => `<span style="display:inline-block; background:rgba(0,212,255,0.12); color:var(--primary); font-size:0.72rem; padding:0.2rem 0.55rem; border-radius:20px; border:1px solid rgba(0,212,255,0.25); font-weight:500;">${c}</span>`).join(' ');
            
            card.innerHTML = `
                <div onclick="if(typeof openBookDetails === 'function') openBookDetails('${book.id}')" style="cursor:pointer;">
                    <img src="${coverUrl}" alt="Portada de ${book.title}" class="book-cover" onerror="this.src='https://via.placeholder.com/220x300?text=Sin+Portada'">
                    <div class="book-info">
                        <h3 class="book-title">${book.title}</h3>
                        <p class="book-author">${book.author}</p>
                        ${tagsHtml ? `<div style="display:flex; flex-wrap:wrap; gap:0.3rem; margin-top:0.4rem;">${tagsHtml}</div>` : ''}
                        <p class="book-price">$${(book.price || 0).toFixed(2)}</p>
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

// --- Lógica del Modal de Detalles del Libro (Global) ---

async function openBookDetails(bookId) {
    let modal = document.getElementById('bookModal');
    let content = document.getElementById('bookModalContent');
    
    if (!modal) {
        modal = document.createElement('div');
        modal.id = 'bookModal';
        modal.className = 'modal';
        modal.style.cssText = 'display:none; position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.8); z-index:1000; align-items:center; justify-content:center; padding: 2rem;';
        
        modal.innerHTML = `
            <div class="modal-content" style="background:var(--surface); padding:2rem; border-radius:12px; width:100%; max-width:800px; max-height:90vh; overflow-y:auto; position:relative;">
                <button id="closeModalBtn" class="btn btn-danger" style="position:absolute; top:1rem; right:1rem;">X</button>
                <div id="bookModalContent"></div>
            </div>
        `;
        document.body.appendChild(modal);
        content = document.getElementById('bookModalContent');
        
        document.getElementById('closeModalBtn').addEventListener('click', () => {
            modal.style.display = 'none';
        });
    }
    
    if (!content) return;
    
    modal.dataset.bookId = bookId;
    content.innerHTML = '<p>Cargando detalles...</p>';
    modal.style.display = 'flex';
    
    try {
        const book = await api.get(`/books/${bookId}`);
        const user = api.auth.getCurrentUser();
        let isFav = false;
        if (user && user.role === 'BUYER') {
            try {
                const favRes = await api.get(`/favorites/check?userId=${user.id}&bookId=${bookId}`);
                isFav = favRes.isFavorite;
            } catch (e) { console.error('Error checando favoritos', e); }
        }
        
        content.innerHTML = `
            <div class="flex gap-2" style="flex-wrap:wrap;">
                <div style="flex:1; min-width:200px;">
                    <img src="${book.coverUrl || 'https://via.placeholder.com/220x300?text=Sin+Portada'}" style="width:100%; border-radius:8px;" onerror="this.src='https://via.placeholder.com/220x300?text=Sin+Portada'">
                </div>
                <div style="flex:2; min-width:300px;">
                    <h2 style="margin-bottom:0.5rem; color:var(--primary); font-size:2rem;">${book.title}</h2>
                    <p style="font-size:1.2rem; margin-bottom:0.5rem; color:var(--text-muted);">${book.author}</p>
                    ${book.categories && book.categories.length > 0 ? `<div style="display:flex; flex-wrap:wrap; gap:0.4rem; margin-bottom:1rem;">${book.categories.map(c => `<span style="background:rgba(0,212,255,0.12); color:var(--primary); font-size:0.8rem; padding:0.25rem 0.7rem; border-radius:20px; border:1px solid rgba(0,212,255,0.3); font-weight:500;">${c}</span>`).join('')}</div>` : ''}
                    <p style="font-size:1.1rem; line-height:1.6; margin-bottom:1.5rem;">${book.description || 'Sin descripción disponible.'}</p>
                    <div style="font-size:1.5rem; font-weight:bold; margin-bottom:1.5rem;">$${(book.price || 0).toFixed(2)}</div>
                    
                    <div class="flex gap-1">
                        <button class="btn btn-primary" style="flex:1;" onclick="addToCart('${book.id}')">🛒 Agregar al Carrito</button>
                        ${user && user.role === 'BUYER' ? `
                            <button class="btn btn-outline" style="flex:1; ${isFav ? 'background:var(--primary); color:white; border-color:var(--primary);' : ''}" onclick="toggleFavorite('${book.id}')">
                                ${isFav ? '♥ En Favoritos' : '♡ Agregar a Favoritos'}
                            </button>
                        ` : ''}
                    </div>
                </div>
            </div>
            <div style="margin-top:2rem; border-top:1px solid var(--surface-light); padding-top:1.5rem;">
                <h3>Reseñas de Usuarios</h3>
                <div id="reviewsList" style="margin-top:1rem;"></div>
            </div>
        `;
        
        loadReviews(bookId);
    } catch (error) {
        content.innerHTML = `<p class="error-message" style="display:block">Error cargando detalles del libro: ${error.message}</p>`;
    }
}


async function loadReviews(bookId) {
    const list = document.getElementById('reviewsList');
    if (!list) return;
    
    try {
        list.innerHTML = '<p>Cargando reseñas...</p>';
        const reviews = await api.get(`/reviews/book/${bookId}`);
        if (!reviews || reviews.length === 0) {
            list.innerHTML = '<p style="color:var(--text-muted);">Sin reseñas aún.</p>';
            return;
        }
        
        let html = '';
        const currentUser = api.auth.getCurrentUser();
        reviews.forEach(rev => {
            let stars = '';
            for(let i=0; i<5; i++) { stars += i < rev.rating ? '★' : '☆'; }
            const showReport = currentUser && currentUser.role === 'BUYER' && rev.user && rev.user.id !== currentUser.id;
            html += `
                <div style="background:var(--background); padding:1rem; border-radius:8px; margin-bottom:1rem; border-left:4px solid var(--primary);">
                    <div class="flex space-between">
                        <strong>${rev.user && rev.user.fullName ? rev.user.fullName : 'Usuario'}</strong>
                        <div>
                            <span style="color:gold;">${stars}</span>
                            ${showReport ? `<button class="btn btn-outline" style="font-size:0.7rem; padding:0.2rem 0.5rem; border-color:var(--danger); color:var(--danger); margin-left:1rem;" onclick="reportReview('${rev.id}')">Reportar</button>` : ''}
                        </div>
                    </div>
                    <p style="margin-top:0.5rem; color:var(--text-muted);">${rev.comment || ''}</p>
                </div>
            `;
        });
        list.innerHTML = html;
    } catch (error) {
        list.innerHTML = `<p class="error-message">Error cargando reseñas.</p>`;
    }
}

window.reportReview = async function(reviewId) {
    if(!confirm("¿Deseas reportar esta reseña a los administradores?")) return;
    try {
        await api.post(`/reviews/${reviewId}/report`);
        alert("Reseña reportada exitosamente. Un administrador la revisará.");
    } catch(e) {
        alert("Error al reportar la reseña: " + e.message);
    }
}
