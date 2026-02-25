# campusflow

Bu proje, Dağıtık Sistemler laboratuvar derslerinde kullanılmak üzere hazırlanmış, başlangıçta monolitik mimariye sahip bir tam yığın (full-stack) uygulamadır. Amaç, çalışan bir sistemi adım adım dağıtık mimariye dönüştürerek dayanıklılık, tutarlılık ve gözlemlenebilirlik kavramlarını uygulamalı olarak incelemektir.

## Stack
- Backend: Spring Boot 3.x, Java 21, Maven, PostgreSQL, Flyway, JPA
- Frontend: React + Vite + TypeScript
- Infra: Docker Compose

## Gereksinimler
- Docker + Docker Compose
- (Optional local backend tests) Java 21 and Maven 3.9+

## Projeyi Çalıştırmak İçin:
```bash
cd campusflow-monolith
docker compose up --build
```

## URLs
- Frontend: http://localhost:8087
- Backend health: http://localhost:8088/health
- Backend actuator health: http://localhost:8088/actuator/health

## Ders Listesi Doğrulama
```bash
curl http://localhost:8088/api/v1/courses
```

## Projenin Amacı (Ders Kapsamı)
Bu proje yalnızca bir ders kayıt uygulaması değildir. Amaç:
- Monolitik bir sistemi incelemek
- Transaction sınırlarını anlamak
- Ardından sistemi kontrollü biçimde dağıtık hâle getirmek
- Dağıtık sistemlerin doğurduğu sorunları gerçek kod üzerinde gözlemlemek

Başlangıçta sistem:
- Tek backend uygulamasıdır
- Tek veritabanı kullanır
- İşlemler tek transaction içinde tamamlanır
- Ağ belirsizliği yoktur
- Bu haliyle sistem dağıtık değildir.
- Laboratuvar süreci boyunca bu yapı kademeli olarak evrimleştirilecektir.

## Proje Evriminde Yapılacak Teknik Çalışmalar
Aşağıdaki dönüşümler bu proje üzerinde gerçekleştirilecektir:

### Monolitin Sağlamlaştırılması
- Structured logging eklenmesi
- Correlation-ID mekanizması
- Gecikme (delay) ve jitter enjeksiyonu
- p95 / p99 gecikme ölçümü
- Timeout ve deadline desteği
- Idempotency mekanizması

### Kod İçi Sınırların Netleştirilmesi
-Enrollment, Capacity ve Audit modüllerinin ayrıştırılması
- Veri sahipliğinin tanımlanması
- Servis sınırlarının belgelenmesi

### Servis Ayrımı ve Gerçek Dağıtıklık
- Capacity bileşeninin ayrı bir servis haline getirilmesi
- Ayrı veritabanı kullanılması
- HTTP üzerinden servisler arası iletişim
- Ağ gecikmesi ve hata simülasyonu

### Dayanıklılık Mekanizmaları
- Timeout yapılandırması
- Retry (exponential backoff + jitter)
- Circuit breaker
- Fallback tasarımı
- Bulkhead izolasyonu
- Rate limiting
- Backpressure yaklaşımı

### Asenkron Mimariye Geçiş
- Mesaj broker entegrasyonu
- Event üretimi ve tüketimi
- Saga iş akışı
- Compensation mekanizmaları
- Outbox pattern

### Mesaj Dayanıklılığı
- Retry policy
- Dead Letter Queue (DLQ)
- Poison message senaryosu
- Consumer idempotency

### Gözlemlenebilirlik
- Distributed tracing
- Prometheus metrikleri
- Grafana dashboard
- Golden signals (latency, traffic, errors, saturation)

### Kontrollü Arıza Deneyleri
- Container kill senaryoları
- Network latency injection
- Broker gecikmesi
- Database latency testleri
- Önce/sonra karşılaştırmalı ölçüm

### Tutarlılık ve Konsensüs
- Servis replikasyonu
- Yük dengeleme davranışı
- Partition senaryoları
- Quorum deneyleri
- Raft ile lider seçimi
- Log replikasyonu
- Hash-chain tabanlı audit doğrulaması

# Nihai Hedef
- Projenin sonunda sistem:
- Çoklu Docker container üzerinde çalışır
- Servisler bağımsız arızalanabilir
- Ağ gecikmesine dayanıklıdır
- Asenkron iş akışını destekler
- Retry fırtınasına karşı korunmuştur
- Mesaj kaybını yönetebilir
- Dağıtık izlenebilirlik sağlar
- Tutarlılık tercihleri açıkça belirlenmiştir
- Audit kayıtları doğrulanabilir hâle getirilmiştir