# 자산이음 (Asset Ieum)
<img width="1672" height="941" alt="자산이음" src="https://github.com/user-attachments/assets/e82d78ca-fde5-4258-8afd-9816be98a482" />

## 📍 프로젝트 소개
**자산이음(Asset-Ieum)** 은 기업 내 유·무형 자산의 전 생애주기를 통합 관리하는 **Enterprise Asset Management Platform**입니다.

기업 내 자산 요청, 승인, 구매, 지급, 반납, 회수 과정을 하나의 플랫폼에서 관리하며, 분산되어 관리되던 자산 정보를 통합하여 운영 효율성과 데이터 정합성을 향상시킵니다.

또한, **HR 시스템 연동, 실시간 알림, 자동화 배치 프로세스**를 통해 자산 라이프사이클 전 과정을 자동화하여 기업 운영 비용 절감과 업무 효율 극대화를 지원합니다.

### ✨ 핵심 특징

- 🏢 유·무형 자산 통합 관리
- 🔄 HR 연동 기반 자산 라이프사이클 자동화
- 🎫 티켓 기반 자산 운영 프로세스
- 🔔 SSE + Redis Pub/Sub 기반 실시간 알림
- ⚙️ Spring Batch 기반 운영 자동화
- 📊 데이터 기반 운영 대시보드
- 📝 감사 로그 및 운영 이력 관리

### 🌐 서비스 링크

| 구분 | 링크 |
|------|------|
| 🌍 Service | https://assetieum.com |

---

## 이음새 팀원 소개
<table align="center">
  <tr>
    <td align="center">
      <img src="images/준석.png" width="130" height="130" alt="양준석" /><br />
      <b>양준석</b><br />
      <a href="https://github.com/YJunSuk">
      <img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white"/>
      </a>
    </td>
    <td align="center">
      <img src="images/희주.png" width="130" height="130" alt="모희주" /><br />
      <b>모희주</b><br />
      <a href="https://github.com/heejudy">
      <img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white"/>
      </a>
    </td>
    <td align="center">
      <img src="images/애은.png" width="130" height="130" alt="이애은" /><br />
      <b>이애은</b><br />
      <a href="https://github.com/nueeaeel">
      <img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white"/>
      </a>
    </td>
    <td align="center">
      <img src="images/민경.png" width="130" height="130" alt="이민경" /><br />
      <b>이민경</b><br />
      <a href="https://github.com/alskung1101">
      <img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white"/>
      </a>
    </td>
  </tr>
</table>
<br>

## 목차

1. [📍 주요 기능](#1-주요-기능)
2. [🛠 기술 스택](#2-기술-스택)
3. [🏗 시스템 아키텍처](#3-시스템-아키텍처)
4. [📋 요구사항 명세서](#4-요구사항-명세서)
5. [🗂 테이블 명세서](#5-테이블-명세서)
6. [🧩 ERD](#6-erd)
7. [🎨 화면 기능 설계서](#7-화면-기능-설계서)
8. [⚙️ 프로그램 사양서](#8-프로그램-사양서)
9. [🖥 상세 서비스 화면](#9-상세-서비스-화면)
10. [🧪 테스트 보고서](#10-테스트-보고서)
11. [🔗 통합 테스트 결과서](#11-통합-테스트-결과서)
12. [🚀 CI/CD 계획서](#12-cicd-계획서)
13. [🔥 트러블 슈팅](#13-트러블-슈팅)
14. [📝 회고](#14-회고)

<br>

---

## 1. 주요 기능
| 아이콘 | 기능 | 설명 |
|:---:|---|---|
| 👥 | 조직 관리 | 회사 / 부서 / 사원 / 권한 관리 |
| 💼 | 자산 관리 | 유형·무형 자산 등록, 배정, 반납, 회수 및 이력 관리 |
| 🛒 | 구매 관리 | 구매 요청, 구매 계획, 구매 정책, 예산 및 집행 이력 관리 |
| 🎫 | 티켓 관리 | 표준 자산 요청, 비표준 구매 요청, 직접 구매, 대여, 유지보수, 반납/해지, 반품/환불 프로세스 관리 |
| 🔄 | HR 연동 자동화 | 입사·퇴사·부서 이동 이벤트 기반 자산 배정, 회수, 티켓 자동 처리 |
| 📋 | 전수조사 | QR 기반 자산 실사, 응답 수집, 미응답 알림 및 후속 조치 관리 |
| 🔔 | 알림 시스템 | SSE 기반 실시간 알림, 티켓 승인/반려/처리 이벤트 전파 |
| 📊 | 대시보드 | 자산 보유 현황, 만료 예정 자산, 티켓 진행률, 예산 현황, HR 이벤트 등 운영 KPI 제공 |
| 📝 | 감사 및 로그 | 사용자 활동 로그, 감사 추적, 주요 업무 변경 이력 관리 |



<br>

## 2. 기술 스택
### 🔧 Backend
<p>
  <img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white">
  <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
  <img src="https://img.shields.io/badge/spring batch-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
  <img src="https://img.shields.io/badge/Spring Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white">
  <img src="https://img.shields.io/badge/jwt-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white">
  <img src="https://img.shields.io/badge/Spring Data JPA-000000?style=for-the-badge&logo=apache&logoColor=white">
</p>

### 🗄️ Database
<p>
  <img src="https://img.shields.io/badge/mariadb-003545?style=for-the-badge&logo=mariadb&logoColor=white">
  <img src="https://img.shields.io/badge/redis-DC382D?style=for-the-badge&logo=redis&logoColor=white">
  <img src="https://img.shields.io/badge/flyway-CC0200?style=for-the-badge&logo=flyway&logoColor=white">
</p>

### 🚀 Infra
<p>
  <img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white">
  <img src="https://img.shields.io/badge/docker--compose-2496ED?style=for-the-badge&logo=docker&logoColor=white">
  <img src="https://img.shields.io/badge/aws cloudfront-F38020?style=for-the-badge&logo=cloudflare&logoColor=white">
  <img src="https://img.shields.io/badge/aws-F38020?style=for-the-badge&logo=cloudflare&logoColor=white">
  

</p>

### ⚙️ CI/CD
<p>
  <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white">
  <img src="https://img.shields.io/badge/github%20actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white">
</p>

### 📊 Monitoring
<p>
  <img src="https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white">
  <img src="https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white">
</p>

### 🤝 Collaboration
<p>
  <img src="https://img.shields.io/badge/notion-000000?style=for-the-badge&logo=notion&logoColor=white">
  <img src="https://img.shields.io/badge/figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white">
  <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white">
  <img src="https://img.shields.io/badge/erdcloud-0B4F6C?style=for-the-badge&logo=icloud&logoColor=white">
  <img src="https://img.shields.io/badge/discord-5865F2?style=for-the-badge&logo=discord&logoColor=white">
</p>
<br/>

<br>

## 3. 시스템 아키텍처
<img width="100%" alt="image" src="https://github.com/user-attachments/assets/eba26bed-48a7-41b4-a64a-fa4d701748e7" />



<br>

## 4. 요구사항 명세서

- [ 요구사항 명세서 ](https://docs.google.com/spreadsheets/d/1z-TKzG4AP284DwpEWPyhNDyRp2_YIFeRTpRxHoSSBkk/edit?gid=683579060#gid=683579060)


<br>

## 5. 테이블 명세서 
- [ 테이블 명세서 ](https://docs.google.com/spreadsheets/d/1z-TKzG4AP284DwpEWPyhNDyRp2_YIFeRTpRxHoSSBkk/edit?gid=2140880264#gid=2140880264)



<br>

## 6. ERD
- [ERD Cloud](https://www.erdcloud.com/d/jHtNFPTxxwohPjgER)
<img width="100%" alt="자산이음_ERD" src="https://github.com/user-attachments/assets/3d87a248-aa49-4d0a-835b-f6d44a3ca510" />

<br>

## 7. 화면 기능 설계서 
- [ 화면 기능 설계서 ](https://www.figma.com/design/i5o7Xeb6cELARDILAXW309/%EC%9E%90%EC%82%B0-%EC%9D%B4%EC%9D%8C?node-id=0-1&p=f&t=1X5kLdHK34VusLdq-0)


<br>

## 8. 프로그램 사양서
- [프로그램 사양서](https://rp3hgdqcg2.apidog.io/)

<br>

## 9. 상세 서비스 화면 
- [ 상세 서비스 화면 ](https://github.com/beyond-sw-camp/be25-fin-AssetIeum-Ieumsae-be/wiki/%EC%83%81%EC%84%B8-%EC%84%9C%EB%B9%84%EC%8A%A4-%ED%99%94%EB%A9%B4)

<br>

## 10. 테스트 보고서 
- [백엔드 단위 테스트 보고서](https://docs.google.com/spreadsheets/d/1z-TKzG4AP284DwpEWPyhNDyRp2_YIFeRTpRxHoSSBkk/edit?gid=481770483#gid=481770483)

- [프론트엔드 단위 테스트 보고서](https://docs.google.com/spreadsheets/d/1z-TKzG4AP284DwpEWPyhNDyRp2_YIFeRTpRxHoSSBkk/edit?gid=324859763#gid=324859763)

<br>

## 11. 통합 테스트 결과서 
- [통합 테스트 결과서](https://github.com/beyond-sw-camp/be25-fin-AssetIeum-Ieumsae-be/wiki/%ED%86%B5%ED%95%A9-%ED%85%8C%EC%8A%A4%ED%8A%B8-%EA%B2%B0%EA%B3%BC%EC%84%9C)

<br>

## 12. CI/CD 계획서 
- [CI/CD 계획서](https://github.com/beyond-sw-camp/be25-fin-AssetIeum-Ieumsae-be/wiki/CICD-%EA%B3%84%ED%9A%8D%EC%84%9C)

<br>


## 13. 트러블 슈팅
- [트러블 슈팅](https://github.com/beyond-sw-camp/be25-fin-AssetIeum-Ieumsae-be/wiki/%ED%8A%B8%EB%9F%AC%EB%B8%94-%EC%8A%88%ED%8C%85)

<br>

## 14. 회고

<details>
<summary><b>양준석</b></summary>
자산이음 프로젝트를 진행하면서 단순히 자산 등록과 조회 기능에 그치지 않고, 자산 요청부터 승인, 재고 확인, 구매 계획 수립, 발주, 납품, 자산 등록, 지급, 반납에 이르는 전체 업무 흐름을 하나의 시스템으로 연결하는 소중한 경험을 했습니다. 저는 팀장과 프론트엔드 개발을 맡아 티켓 승인 및 처리 프로세스, 구매 계획과 발주 흐름, 운영 리포트, 권한과 상태에 따른 화면 제어, API 연동 등 업무를 담당하였습니다. 개발하는 과정에서는 재고 보유 여부에 따른 프로세스 분기, 티켓 상태와 구매 계획 상태의 분리, 역할별 접근 권한, 동시 승인과 중복 자산 배정을 방지하기 위한 데이터 정합성 문제를 중점적으로 고민하였습니다.
<br>
또한 프론트엔드에서 단순히 버튼을 숨기는 것만으로는 보안을 완벽히 보장할 수 없다는 점과, 화면을 구현하기 전에 업무 상태와 예외 흐름을 먼저 명확히 정의해야 한다는 점을 배웠습니다. 프로젝트 후반에는 AWS 기반 배포 구조와 CI/CD, Auto Scaling 환경에서 파일 저장과 세션 관리, 시연 영상과 발표 자료 구성까지 경험하며 기획, 설계, 개발, 배포, 발표로 이어지는 전체 프로젝트 과정을 폭넓게 이해할 수 있었습니다.
<br>
특히 여러 기능을 많이 구현하는 것보다 실제 사용자와 운영자의 관점에서 하나의 프로세스가 끊김 없이 동작하도록 만드는 것이 더 중요하다는 점을 배울 수 있었습니다.
<br>
팀원분들 모두 정말 고생 많으셨습니다. 한 분이라도 함께하지 않았다면 지금과 같은 결과물을 만들기는 어려웠을 것 같습니다. 각자의 자리에서 끝까지 최선을 다해주신 덕분에 프로젝트를 잘 마무리할 수 있었습니다.
저 역시 팀장으로서 많이 배우고 성장할 수 있었던 시간이었습니다. 부족한 점도 많았지만 믿고 함께해주셔서 진심으로 감사드립니다. 그동안 정말 고생 많으셨습니다!

</details>


<details>
<summary><b>모희주</b></summary>
이번 프로젝트에서는 단순히 개발뿐만 아니라 기획과 협업의 중요성도 많이 배울 수 있었습니다. 특히 프로젝트에서 개발만큼이나 기획 단계에서 뼈대를 탄탄하게 잡는 것이 중요하다는 것을 느꼈습니다. 앞으로도 개발이 들어가기 전에 문서들을 탄탄히 잡아두고 프로젝트에 대한 이해도를 높이도록 노력해야겠다고 생각했습니다.
<br>
개발 측면에서는 컴포넌트 재사용 구조를 고민하며 화면을 구현했고, Mock 데이터와 실제 API를 연동하는 과정을 경험하며 전체 개발 과정을 이해할 수 있었습니다. 또한 개발 과정에서 발생한 다양한 트러블을 팀원들과 함께 원인을 찾아 해결해 나가며 소통과 협업의 중요성을 다시 한번 느낄 수 있었습니다.
<br>
마지막 프로젝트를 함께 할 수 있어서 좋았습니다. 팀원분들 덕분에 정말 많이 배우고 많은 것을 느낄 수 있었던 것 같습니다. 정말 수고많으셨습니다. 
</details>


<details>
<summary><b>이애은</b></summary>
  이번 프로젝트에서는 자산 도메인 관리, 구매 프로세스, 전수조사 기능, HR 이벤트 자동화 및 Batch 개발, 모니터링 환경 구축을 담당하였습니다. 단순히 자산을 등록하고 조회하는 기능을 구현하는 것을 넘어, 요청부터 승인, 구매, 지급, 반납까지 이어지는 자산의 전 생애주기를 시스템으로 설계하며 실제 기업 환경에서 사용될 수 있는 서비스를 구현해볼 수 있었습니다. 특히 표준·비표준·직접 구매와 같은 다양한 구매 유형과 입사, 퇴사, 부서 이동에 따른 HR 이벤트 자동화 로직을 구현하면서 복잡한 비즈니스 요구사항을 도메인 중심으로 설계하는 경험을 쌓을 수 있었습니다.

또한 Spring Batch를 활용하여 반복적인 업무를 자동화하는 과정을 통해 운영 효율성을 높이는 시스템 설계의 중요성을 체감하였으며, Auto Scaling, ALB, Redis, CloudFront 등을 활용한 클라우드 환경 구성과 GitHub Actions 기반 CI/CD 파이프라인 구축, Grafana와 Prometheus를 활용한 모니터링 환경 구축을 경험하면서 기능 개발뿐만 아니라 안정적인 서비스 운영 역시 백엔드 개발자의 중요한 역할임을 배울 수 있었습니다.

이번 프로젝트를 통해 단순히 기능을 구현하는 개발자를 넘어 비즈니스 요구사항을 시스템으로 설계하고 안정적으로 운영할 수 있는 백엔드 개발자로 한 단계 성장할 수 있었습니다. 앞으로도 서비스의 안정성과 운영 효율을 함께 고민하며, 실제 현업에서 활용할 수 있는 시스템을 설계하고 구현하는 개발자로 성장해 나가고자 합니다.

마지막으로 약 두 달 동안 함께 고민하고 협업하며 프로젝트를 완성해낸 팀 이음새 팀원분들 모두 정말 고생 많으셨습니다. 끝까지 열심히 함께 해주셔서 감사합니다!!
<br>

</details>


<details>
<summary><b>이민경</b></summary>
  최종 프로젝트를 진행하면서 가장 어려웠던 점은 기능을 구현하는 것보다 프로젝트의 전체 흐름을 맞춰가는 과정이었다. 처음에는 자산 관리 업무와 도메인을 이해하는 것부터 쉽지 않았고 기능이 많다 보니 각 기능이 어떤 순서로 연결되고 어떤 데이터를 주고받는지 파악하는 데도 시간이 필요했다. 팀원들과 요구사항을 계속 정리하고 의견을 맞춰가며 기능을 하나씩 구현하면서 프로젝트를 완성해 나갔다.

담당 파트였던 백엔드에서는 자산 요청, 구매 계획, 자산 배정, 대여·반납, 알림 등 여러 기능이 서로 연결되어 있어 단순히 CRUD를 구현하는 것보다 상태 흐름과 데이터가 올바르게 연결되는지를 계속 확인해야 했다. 특히 자산 요청은 재고가 있으면 바로 배정하고 부족하면 구매 계획으로 이어지는 흐름을 구현하면서 예산, 재고, 배정 이력이 함께 변경되는 부분을 여러 번 수정하고 보완했다. 또한 프론트엔드와 API를 연동하는 과정에서는 응답 형식과 예외 처리, 상태값을 맞추기 위해 여러 차례 수정 작업을 진행했고 서로의 요구사항을 맞춰가는 과정의 중요성도 느낄 수 있었다.

배포 과정에서는 ECR과 Auto Scaling Group 기반으로 환경을 구성하며 운영 환경을 직접 확인했고 인증 오류와 시간대 문제를 해결하면서 로컬에서 정상적으로 동작하던 기능도 운영 환경에서는 예상하지 못한 문제가 발생할 수 있다는 점을 경험했다. 특히 컨테이너가 실제로 새로 생성되었는지, 배포가 정상적으로 반영되었는지, 환경 설정이 올바르게 적용되었는지를 직접 확인하면서 기능 구현만큼 운영 환경을 점검하는 과정도 중요하다는 것을 느꼈다.

이번 프로젝트를 진행하면서 기능 하나를 구현하는 것보다 여러 기능이 자연스럽게 연결되고 실제 운영 환경에서도 안정적으로 동작하도록 만드는 과정이 더 어렵다는 것을 느꼈다. 개발뿐 아니라 협업 과정에서 요구사항을 조율하고 배포 이후에도 문제를 확인하고 해결하는 경험까지 할 수 있었던 점이 가장 기억에 남았고 이번 프로젝트를 통해 백엔드 개발 전반에 대해 한 단계 더 깊게 경험할 수 있었다. 팀원분들도 그동안 정말 수고 많으셨습니다!!

<br>

</details>

<br>

---

<br>

## 그 외 산출물

### 프로젝트 문서
- [WBS](https://docs.google.com/spreadsheets/d/1z-TKzG4AP284DwpEWPyhNDyRp2_YIFeRTpRxHoSSBkk/edit?gid=0#gid=0)
- [기획서](./docs/자산이음%20기획서.pdf)
