# ⚽ Easy Sports Club V2 ⚽
[이전 프로젝트](https://github.com/MinWonHaeSo)의 배포환경에 대한 가용성 개선 및 성능 테스트 프로젝트

---
## 소개

기존 Easy Sports Club 프로젝트를 아키텍처 관점에서 고민하여<br>
가용성을 개선하고, 성능 테스트를 통해 이를 수치로 확인하여<br>
프로젝트를 한 단계 더 발전시키기 위한 개선 프로젝트<br>

---

## 개선 사항
- Scale Out을 통한 기존 프로젝트 가용성 개선 및 Jmeter 성능 테스트
  - (AS-IS) 웹x1 + 앱x1 + DBx1 - (각 vCPU 1 / RAM 1GB)
  - (AS-IS) 웹x2 + 앱x3 + DBx2 - (각 vCPU 2 / RAM 4GB)
- 데이터베이스 읽기, 쓰기 이중화
- 서비스 로직으로 처리하던 엘라스틱서치 데이터 마이그레이션을 Logstash Pipeline을 이용하여 자동화
- 모니터링 및 Slack 알림 시스템 구성
