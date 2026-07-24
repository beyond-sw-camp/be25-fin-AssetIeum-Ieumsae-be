import http from 'k6/http';
import { check, fail, sleep } from 'k6';

const BASE_URL = (__ENV.BASE_URL || 'http://host.docker.internal:8080').replace(/\/$/, '');
const ACCESS_TOKEN = __ENV.ACCESS_TOKEN || '';
const LOGIN_CREDENTIALS = {
  companyCode: __ENV.COMPANY_CODE || 'assetieum',
  memberNo: __ENV.MEMBER_NO || 'M001',
  password: __ENV.PASSWORD || 'M001',
};

export const options = {
  stages: [
    { duration: '1m', target: 50 },
    { duration: '2m', target: 50 },
    { duration: '1m', target: 100 },
    { duration: '2m', target: 100 },
    { duration: '1m', target: 200 },
    { duration: '2m', target: 200 },
    { duration: '1m', target: 300 },
    { duration: '2m', target: 300 },
    { duration: '2m', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<2000'],
    checks: ['rate>0.95'],
  },
};

export function setup() {
  if (ACCESS_TOKEN) {
    return { accessToken: ACCESS_TOKEN };
  }

  const response = http.post(
    `${BASE_URL}/api/v1/auth/login`,
    JSON.stringify(LOGIN_CREDENTIALS),
    {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'POST /api/v1/auth/login' },
      timeout: '10s',
    },
  );

  if (response.status !== 200) {
    fail(`Login failed: status=${response.status}, body=${response.body}`);
  }

  let body;
  try {
    body = response.json();
  } catch (error) {
    fail(`Failed to parse login response: ${error.message}`);
  }

  const accessToken = body && body.data && body.data.accessToken;
  if (!accessToken) {
    fail(`Access token is missing in login response: ${response.body}`);
  }

  return { accessToken };
}

export default function (data) {
  const response = http.get(`${BASE_URL}/api/v1/tickets`, {
    headers: { Authorization: `Bearer ${data.accessToken}` },
    tags: { name: 'GET /api/v1/tickets' },
    timeout: '10s',
  });

  check(response, {
    'response status is 200': (res) => res.status === 200,
    'response time is under 5 seconds': (res) => res.timings.duration < 5000,
  });

  sleep(0.5);
}
