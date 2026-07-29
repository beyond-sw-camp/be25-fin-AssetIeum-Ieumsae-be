import http from 'k6/http';
import { check, fail, sleep } from 'k6';

const BASE_URL = (__ENV.BASE_URL || 'http://host.docker.internal:8081').replace(/\/$/, '');
const MODE = __ENV.TEST_MODE || 'unknown';

export const options = {
  stages: [
    { duration: '10s', target: 30 },
    { duration: '45s', target: 30 },
    { duration: '10s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<2000'],
    checks: ['rate>0.99'],
  },
  tags: {
    test_mode: MODE,
  },
};

export function setup() {
  const response = http.post(
    `${BASE_URL}/api/v1/auth/login`,
    JSON.stringify({
      companyCode: __ENV.COMPANY_CODE,
      memberNo: __ENV.MEMBER_NO,
      password: __ENV.PASSWORD,
    }),
    {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'POST /api/v1/auth/login' },
    },
  );

  if (response.status !== 200) {
    fail(`Login failed: status=${response.status}`);
  }

  const accessToken = response.json('data.accessToken');
  if (!accessToken) {
    fail('Access token is missing');
  }
  return { accessToken };
}

export default function (data) {
  const params = {
    headers: { Authorization: `Bearer ${data.accessToken}` },
  };
  const responses = http.batch([
    ['GET', `${BASE_URL}/api/v1/dashboard/ticket-progress`, null, {
      ...params,
      tags: { name: 'GET /api/v1/dashboard/ticket-progress' },
    }],
    ['GET', `${BASE_URL}/api/v1/dashboard/owned-assets`, null, {
      ...params,
      tags: { name: 'GET /api/v1/dashboard/owned-assets' },
    }],
    ['GET', `${BASE_URL}/api/v1/dashboard/expiring-assets`, null, {
      ...params,
      tags: { name: 'GET /api/v1/dashboard/expiring-assets' },
    }],
  ]);

  responses.forEach((response) => {
    check(response, {
      'dashboard response is 200': (result) => result.status === 200,
    });
  });
  sleep(0.2);
}
