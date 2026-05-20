// buyer.js
// Contiene la lógica del dashboard del comprador: Perfil, Historial de Compras, Favoritos y Modal de Detalles del Libro

document.addEventListener('DOMContentLoaded', () => {
    // --- Lógica del Perfil ---
    const profileForm = document.getElementById('profileForm');
    if (profileForm) {
        profileForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const user = api.auth.getCurrentUser();
            const msgDiv = document.getElementById('profileMessage');
            try {
                const res = await api.put(`/users/${user.id}/profile`, {
                    fullName: document.getElementById('profName').value,
                    phone: document.getElementById('profPhone').value,
                    address: document.getElementById('profAddress').value
                });
                msgDiv.textContent = 'Perfil actualizado con éxito.';
                msgDiv.style.color = 'var(--primary)';
                document.getElementById('userInfo').textContent = `Hola, ${res.fullName}`;
            } catch (error) {
                msgDiv.textContent = error.message;
                msgDiv.style.color = 'var(--danger)';
            }
        });
    }

    // Modal de Detalles de Libro
    const closeModalBtn = document.getElementById('closeModalBtn');
    if (closeModalBtn) {
        closeModalBtn.addEventListener('click', () => {
            document.getElementById('bookModal').style.display = 'none';
        });
    }

    // Enviar Reseña
    const submitReviewBtn = document.getElementById('submitReviewBtn');
    if (submitReviewBtn) {
        submitReviewBtn.addEventListener('click', async () => {
            const user = api.auth.getCurrentUser();
            const bookId = document.getElementById('bookModal').dataset.bookId;
            const rating = parseInt(document.getElementById('reviewRating').value);
            const comment = document.getElementById('reviewComment').value;
            
            if (!bookId) return;
            
            try {
                await api.post('/reviews', {
                    userId: user.id,
                    bookId: bookId,
                    rating: rating.toString(),
                    comment: comment
                });
                alert('Reseña enviada con éxito');
                document.getElementById('reviewComment').value = '';
                loadReviews(bookId);
            } catch (error) {
                alert('Error al enviar reseña: ' + error.message);
            }
        });
    }
});

// --- Funciones de Carga de Vistas ---

async function loadProfile() {
    const user = api.auth.getCurrentUser();
    if (!user) return;
    try {
        const profile = await api.get(`/users/${user.id}/profile`);
        document.getElementById('profName').value = profile.fullName || '';
        document.getElementById('profPhone').value = profile.phone || '';
        document.getElementById('profAddress').value = profile.address || '';
    } catch (error) {
        console.error('Error cargando perfil:', error);
    }
}

async function loadOrders() {
    const user = api.auth.getCurrentUser();
    const container = document.getElementById('ordersList');
    try {
        container.innerHTML = '<p>Cargando compras...</p>';
        const page = await api.get(`/orders/history/${user.id}`);
        // Endpoint devuelve una página de Spring Data: page.content
        const orders = page.content || [];
        
        if (!orders || orders.length === 0) {
            container.innerHTML = '<p>Aún no has realizado compras.</p>';
            return;
        }

        let html = '<div style="display:flex; flex-direction:column; gap:1rem;">';
        orders.forEach(order => {
            html += `
                <div style="background:var(--surface); padding:1.5rem; border-radius:8px; border:1px solid var(--surface-light);">
                    <div class="flex space-between" style="align-items:center; margin-bottom: 1rem;">
                        <div>
                            <strong>Orden #${order.id.substring(0, 8)}</strong>
                            <span style="color:var(--text-muted); margin-left:1rem;">${order.fecha || 'Reciente'}</span>
                        </div>
                        <span style="color:var(--primary); font-weight:bold; font-size: 1.2rem;">$${(order.totalAmount || 0).toFixed(2)}</span>
                    </div>
                    <p style="font-size:0.9rem; color:var(--text-muted);">Estado: ${order.status || 'COMPLETADA'}</p>
                </div>`;
        });
        html += '</div>';
        container.innerHTML = html;
    } catch (error) {
        container.innerHTML = `<p class="error-message" style="display:block">${error.message}</p>`;
    }
}

async function loadFavorites() {
    const user = api.auth.getCurrentUser();
    const grid = document.getElementById('favoritesGrid');
    try {
        grid.innerHTML = '<p>Cargando favoritos...</p>';
        const favorites = await api.get(`/favorites/${user.id}`);
        
        if (!favorites || favorites.length === 0) {
            grid.innerHTML = '<p>Aún no tienes libros favoritos.</p>';
            return;
        }

        grid.innerHTML = '';
        favorites.forEach(fav => {
            const card = document.createElement('div');
            card.className = 'book-card';
            card.innerHTML = `
                <div style="background:var(--surface-light); height:220px; display:flex; align-items:center; justify-content:center; cursor:pointer;" onclick="openBookDetails('${fav.bookId}')">
                    <span style="font-size:3rem">📚</span>
                </div>
                <div class="book-info">
                    <h3 class="book-title" style="cursor:pointer;" onclick="openBookDetails('${fav.bookId}')">Libro Favorito</h3>
                    <p style="font-size:0.8rem; color:var(--text-muted);">Haz clic para ver detalles</p>
                    <button class="btn btn-outline mt-1" style="width:100%; border-color:var(--danger); color:var(--danger);" onclick="toggleFavorite('${fav.bookId}', true)">Quitar de Favoritos</button>
                </div>
            `;
            // Como el backend de Favoritos tal vez no devuelve la info completa del libro en el endpoint listar, 
            // mostramos un botón para ver los detalles. Si devuelven el book entero, se podría renderizar normal.
            grid.appendChild(card);
        });
    } catch (error) {
        grid.innerHTML = `<p class="error-message" style="display:block">${error.message}</p>`;
    }
}

async function toggleFavorite(bookId, reloadFavView = false) {
    const user = api.auth.getCurrentUser();
    if (!user) return;
    
    try {
        // Chequear si es favorito primero
        const res = await api.get(`/favorites/check?userId=${user.id}&bookId=${bookId}`);
        if (res.isFavorite) {
            // Eliminar
            await api.delete(`/favorites?userId=${user.id}&bookId=${bookId}`);
        } else {
            // Agregar
            await api.post('/favorites', { userId: user.id, bookId: bookId });
        }
        
        if (reloadFavView) {
            loadFavorites();
        } else {
            // Solo actualiza el modal si estamos viéndolo
            openBookDetails(bookId); 
        }
    } catch (error) {
        alert(error.message);
    }
}

// --- Lógica del Modal de Detalles del Libro ---

async function openBookDetails(bookId) {
    const modal = document.getElementById('bookModal');
    const content = document.getElementById('bookModalContent');
    if (!modal || !content) return;
    
    modal.dataset.bookId = bookId;
    content.innerHTML = '<p>Cargando detalles...</p>';
    modal.style.display = 'flex';
    
    try {
        const book = await api.get(`/books/${bookId}`);
        const user = api.auth.getCurrentUser();
        let isFav = false;
        if (user && user.role === 'BUYER') {
            const favRes = await api.get(`/favorites/check?userId=${user.id}&bookId=${bookId}`);
            isFav = favRes.isFavorite;
        }
        
        content.innerHTML = `
            <div class="flex gap-2" style="flex-wrap:wrap;">
                <div style="flex:1; min-width:200px;">
                    <img src="${book.urlPortada || 'https://via.placeholder.com/220x300?text=Sin+Portada'}" style="width:100%; border-radius:8px;" onerror="this.src='https://via.placeholder.com/220x300?text=Sin+Portada'">
                </div>
                <div style="flex:2; min-width:300px;">
                    <h2 style="margin-bottom:0.5rem; color:var(--primary); font-size:2rem;">${book.titulo}</h2>
                    <p style="font-size:1.2rem; margin-bottom:1rem; color:var(--text-muted);">${book.autor}</p>
                    <p style="font-size:1.1rem; line-height:1.6; margin-bottom:1.5rem;">${book.descripcion || 'Sin descripción disponible.'}</p>
                    <div style="font-size:1.5rem; font-weight:bold; margin-bottom:1.5rem;">$${book.precio.toFixed(2)}</div>
                    
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
            list.innerHTML = '<p style="color:var(--text-muted);">Sé el primero en dejar una reseña para este libro.</p>';
            return;
        }
        
        let html = '';
        reviews.forEach(rev => {
            let stars = '';
            for(let i=0; i<5; i++) { stars += i < rev.rating ? '★' : '☆'; }
            html += `
                <div style="background:var(--background); padding:1rem; border-radius:8px; margin-bottom:1rem; border-left:4px solid var(--primary);">
                    <div class="flex space-between">
                        <strong>Usuario #${rev.userId ? rev.userId.substring(0,6) : 'N/A'}</strong>
                        <span style="color:gold;">${stars}</span>
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
