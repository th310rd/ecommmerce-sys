import axios from 'axios';

// Use relative base URL; Vite dev server proxies /api to backend (vite.config.ts)
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '';

export const api = axios.create({
    baseURL: apiBaseUrl,
    headers: {
        'Content-Type': 'application/json'
    }
});

// Attach JWT if available
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers = config.headers ?? {};
        (config.headers as any)['Authorization'] = `Bearer ${token}`;
    }
    return config;
});

export type Product = {
    id: number;
    name: string;
    description?: string;
    price: number;
    stock?: number;
};

export type OrderItemInput = {
    productId: number;
    quantity: number;
};

export type Order = {
    id: number;
    status: string;
    createdAt?: string;
    items: Array<{
        productId: number;
        quantity: number;
        price?: number;
    }>;
};

export async function listProducts(): Promise<Product[]> {
    const res = await api.get('/products');
    return res.data;
}

export async function listOrders(): Promise<Order[]> {
    const res = await api.get('/orders');
    return res.data;
}

export async function createOrder(items: OrderItemInput[]): Promise<Order> {
    const res = await api.post('/orders', { orderItems: items });
    return res.data;
}



