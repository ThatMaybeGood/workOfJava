/**
 * ETL API 封装
 */
const ETL_API_BASE = 'http://localhost:18089/api/etl';

const etlApi = {
    async get(url) {
        const res = await fetch(ETL_API_BASE + url);
        return res.json();
    },
    async post(url, data) {
        const res = await fetch(ETL_API_BASE + url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        return res.json();
    },
    async put(url, data) {
        const res = await fetch(ETL_API_BASE + url, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        return res.json();
    },
    async del(url) {
        const res = await fetch(ETL_API_BASE + url, { method: 'DELETE' });
        return res.json();
    }
};