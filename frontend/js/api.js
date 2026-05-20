const API_BASE_URL = 'http://localhost:8080/api';

const api = {
    // Función central para hacer peticiones
    async request(endpoint, options = {}) {
        const url = `${API_BASE_URL}${endpoint}`;
        
        // Recuperar información de sesión (simulando Basic Auth o guardando UUID para el backend)
        const user = JSON.parse(localStorage.getItem('openlib_user') || 'null');
        
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };

        // Si hubiera JWT o Basic Auth lo mandaríamos aquí. 
        // Por ahora pasamos sin Auth estricta a nivel HTTP, el backend verifica UUIDs
        if (user && user.email) {
            headers['Authorization'] = 'Basic ' + btoa(user.email + ':' + localStorage.getItem('openlib_pwd'));
        }

        const config = {
            ...options,
            headers
        };

        try {
            const response = await fetch(url, config);
            
            // 204 No Content
            if (response.status === 204) {
                return null;
            }

            const data = await response.json().catch(() => null);

            if (!response.ok) {
                throw new Error((data && data.error) ? data.error : `Error HTTP: ${response.status}`);
            }

            return data;
        } catch (error) {
            console.error(`Error en API (${endpoint}):`, error);
            throw error;
        }
    },

    // Métodos útiles de acceso rápido
    get(endpoint) { return this.request(endpoint); },
    post(endpoint, body) { return this.request(endpoint, { method: 'POST', body: JSON.stringify(body) }); },
    put(endpoint, body) { return this.request(endpoint, { method: 'PUT', body: JSON.stringify(body) }); },
    delete(endpoint) { return this.request(endpoint, { method: 'DELETE' }); },

    // Funciones de Autenticación
    auth: {
        login: async (email, password) => {
            const result = await api.post('/users/login', { email, password });
            if (result && result.id) {
                localStorage.setItem('openlib_user', JSON.stringify(result));
                localStorage.setItem('openlib_pwd', password); // Para Auth Basic simple
                return result;
            }
            throw new Error("Respuesta inválida del servidor");
        },
        registerBuyer: async (fullName, email, password) => {
            return await api.post('/users/register', { fullName, email, password });
        },
        registerSeller: async (fullName, email, password) => {
            return await api.post('/users/register-seller', { fullName, email, password });
        },
        logout: () => {
            localStorage.removeItem('openlib_user');
            localStorage.removeItem('openlib_pwd');
            window.location.href = 'index.html';
        },
        getCurrentUser: () => JSON.parse(localStorage.getItem('openlib_user'))
    }
};
