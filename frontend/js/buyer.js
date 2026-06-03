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
                        <span style="color:var(--primary); font-weight:bold; font-size: 1.2rem;">$${(order.totalPrice || 0).toFixed(2)}</span>
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
            const bookId = fav.book ? fav.book.id : fav.bookId;
            const bookTitle = fav.book ? fav.book.title : 'Libro Favorito';
            const bookCover = fav.book ? fav.book.coverUrl : '';
            
            const card = document.createElement('div');
            card.className = 'book-card';
            card.innerHTML = `
                <div style="background:var(--surface-light); height:220px; display:flex; align-items:center; justify-content:center; cursor:pointer;" onclick="openBookDetails('${bookId}')">
                    ${bookCover ? `<img src="${bookCover}" style="width:100%;height:100%;object-fit:cover;">` : `<span style="font-size:3rem">📚</span>`}
                </div>
                <div class="book-info">
                    <h3 class="book-title" style="cursor:pointer;" onclick="openBookDetails('${bookId}')">${bookTitle}</h3>
                    <p style="font-size:0.8rem; color:var(--text-muted);">Haz clic para ver detalles</p>
                    <button class="btn btn-outline mt-1" style="width:100%; border-color:var(--danger); color:var(--danger);" onclick="toggleFavorite('${bookId}', true)">Quitar de Favoritos</button>
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


// --- Modal de Reseñas desde la Biblioteca ---

let _currentLibraryBookId = null;

async function openLibraryReviewModal(bookId, title) {
    _currentLibraryBookId = bookId;
    const modal = document.getElementById('libraryModal');
    const contentDiv = document.getElementById('libraryModalContent');
    const reviewsDiv = document.getElementById('libraryReviewsDisplay');
    const msgDiv = document.getElementById('libraryReviewMsg');
    
    if (!modal) return;

    // Mostrar título del libro
    if (contentDiv) {
        contentDiv.innerHTML = `<h2 style="color:var(--primary); margin-bottom:0.5rem;">${title}</h2><p style="color:var(--text-muted);">Escribe tu reseña sobre este libro.</p>`;
    }
    
    // Limpiar formulario
    const comment = document.getElementById('libraryReviewComment');
    const rating = document.getElementById('libraryReviewRating');
    if (comment) comment.value = '';
    if (rating) rating.value = '5';
    if (msgDiv) msgDiv.textContent = '';
    
    // Mostrar reseñas existentes
    if (reviewsDiv) {
        reviewsDiv.innerHTML = '<p style="color:var(--text-muted);">Cargando reseñas...</p>';
        try {
            const reviews = await api.get(`/reviews/book/${bookId}`);
            if (!reviews || reviews.length === 0) {
                reviewsDiv.innerHTML = '<p style="color:var(--text-muted); font-size:0.9rem;">Aún no hay reseñas para este libro. ¡Sé el primero!</p>';
            } else {
                let html = '<div style="display:flex; flex-direction:column; gap:0.75rem;">';
                reviews.forEach(rev => {
                    let stars = '';
                    for(let i=0;i<5;i++) stars += i < rev.rating ? '★' : '☆';
                    html += `
                        <div style="background:var(--surface-light); padding:0.75rem; border-radius:8px; border-left:3px solid var(--primary);">
                            <div style="display:flex; justify-content:space-between;">
                                <strong>${rev.user && rev.user.fullName ? rev.user.fullName : 'Usuario'}</strong>
                                <span style="color:gold;">${stars}</span>
                            </div>
                            <p style="margin-top:0.3rem; color:var(--text-muted); font-size:0.9rem;">${rev.comment || ''}</p>
                        </div>`;
                });
                html += '</div>';
                reviewsDiv.innerHTML = html;
            }
        } catch (e) {
            reviewsDiv.innerHTML = '';
        }
    }
    
    modal.style.display = 'flex';
}

async function submitLibraryReview() {
    const user = api.auth.getCurrentUser();
    const bookId = _currentLibraryBookId;
    const rating = document.getElementById('libraryReviewRating').value;
    const comment = document.getElementById('libraryReviewComment').value;
    const msgDiv = document.getElementById('libraryReviewMsg');
    
    if (!bookId || !user) return;
    if (!comment.trim()) { msgDiv.textContent = 'Escribe un comentario antes de enviar.'; msgDiv.style.color = 'var(--danger)'; return; }
    
    try {
        await api.post('/reviews', {
            userId: user.id,
            bookId: bookId,
            rating: rating.toString(),
            comment: comment.trim()
        });
        msgDiv.textContent = '✅ Reseña enviada con éxito. Gracias por tu opinión.';
        msgDiv.style.color = 'var(--primary)';
        document.getElementById('libraryReviewComment').value = '';
        // Recargar reseñas en el modal
        openLibraryReviewModal(bookId, document.getElementById('libraryModalContent').querySelector('h2')?.textContent || '');
    } catch (error) {
        msgDiv.textContent = '❌ ' + (error.message.includes('Ya has reseñado') ? 'Ya dejaste una reseña para este libro.' : error.message);
        msgDiv.style.color = 'var(--danger)';
    }
}
