import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = (__ENV.BASE_URL || 'http://host.docker.internal:8080')
    .replace(/\/$/, '');

const ACCESS_TOKEN = __ENV.ACCESS_TOKEN || '';

export const options = {
    stages: [
        { duration: '1m', target: 300 },
        { duration: '5m', target: 300 },
        { duration: '1m', target: 0 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<800', 'p(99)<1500'],
        checks: ['rate>0.99'],
    },
};

export default function () {
    const headers = ACCESS_TOKEN
        ? { Authorization: `Bearer ${ACCESS_TOKEN}` }
        : {};

    const requests = ACCESS_TOKEN
        ? [
            [
                'GET',
                `${BASE_URL}/api/v1/health`,
                null,
                {
                    tags: { name: 'GET /api/v1/health' },
                },
            ],
            [
                'GET',
                `${BASE_URL}/api/v1/departments`,
                null,
                {
                    headers,
                    tags: { name: 'GET /api/v1/departments' },
                },
            ],
            [
                'GET',
                `${BASE_URL}/api/v1/tickets`,
                null,
                {
                    headers,
                    tags: { name: 'GET /api/v1/tickets' },
                },
            ],
            [
                'GET',
                `${BASE_URL}/api/v1/dashboard/ticket-progress`,
                null,
                {
                    headers,
                    tags: { name: 'GET /api/v1/dashboard/ticket-progress' },
                },
            ],
        ]
        : [
            [
                'GET',
                `${BASE_URL}/api/v1/health`,
                null,
                {
                    tags: { name: 'GET /api/v1/health' },
                },
            ],
        ];

    const responses = http.batch(requests);

    responses.forEach((response) => {
        check(response, {
            '응답 상태는 200': (res) => res.status === 200,
        });
    });

    sleep(Math.random() * 2 + 1);

}