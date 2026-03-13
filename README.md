# 🏋️ GymTracking

### Modern Fitness & Progressive Overload Assistant

**GymTracking**, sporcuların gelişimlerini bilimsel yöntemlerle (**Progressive Overload**) takip etmelerini sağlayan modern bir **Android fitness uygulamasıdır**.

Kullanıcıların:

* 🏋️ Antrenman programlarını yönetmesini
* 📈 Kişisel rekorlarını (PR) takip etmesini
* 📸 Gelişim fotoğraflarını saklamasını
* 🍎 Beslenme hedeflerini hesaplamasını

sağlayan kapsamlı bir fitness takip uygulamasıdır.

Uygulama **Jetpack Compose** kullanılarak modern Android mimarisi ile geliştirilmiştir.

---

# 📸 Ekran Görüntüleri
app/src/main/res/drawable/screenshots/g1.png
## Ana Ekranlar

<img src="https://raw.githubusercontent.com/SabanCE/GymTracking/app/src/main/res/drawable/screenshots/h1.png" width="250">
<img src="https://raw.githubusercontent.com/SabanCE/GymTracking/main/app/src/main/res/drawable/screenshots/h2.png" width="250">

---

## Program Yönetimi

<img src="https://raw.githubusercontent.com/SabanCE/GymTracking/main/app/src/main/res/drawable/screenshots/p1.png" width="250">
<img src="https://raw.githubusercontent.com/SabanCE/GymTracking/main/app/src/main/res/drawable/screenshots/np1.png" width="250">

---

## Antrenman (Workout) Takibi

<img src="https://raw.githubusercontent.com/SabanCE/GymTracking/main/app/src/main/res/drawable/screenshots/w1.png" width="250">
<img src="https://raw.githubusercontent.com/SabanCE/GymTracking/main/app/src/main/res/drawable/screenshots/w2.png" width="250">

---

## Gelişim Takibi

<img src="https://raw.githubusercontent.com/SabanCE/GymTracking/main/app/src/main/res/drawable/screenshots/g1.png" width="250">

---

## Makro (Kalori) Hesaplama

<img src="https://raw.githubusercontent.com/SabanCE/GymTracking/main/app/src/main/res/drawable/screenshots/m1.png" width="250">

---

# ✨ Öne Çıkan Özellikler

### 📈 Progressive Overload Takibi

Her egzersiz için **bir önceki haftanın set, tekrar ve ağırlık verileri (PR)** otomatik olarak gösterilir.
Bu sayede sporcular gelişimlerini net şekilde takip edebilir.

### 📅 Akıllı Gün Yönetimi

Program günleri tamamlandığında **program başa döner**, ancak **toplam gün sayacı takvim gibi ilerlemeye devam eder**.

### 📂 Egzersiz Kütüphanesi

Kategori bazlı egzersiz sistemi sayesinde veri tutarlılığı korunur.

Kullanıcılar ayrıca:

* özel egzersiz ekleyebilir
* kendi antrenman programlarını oluşturabilir

---

### 📸 Fotoğraf Günlüğü

Gelişim fotoğrafları uygulamanın **internal storage klasörüne kopyalanarak** tarih bazlı saklanır.

Bu sayede fotoğraf galeriden silinse bile uygulamadaki veriler korunur.

---

### 🍎 Beslenme Asistanı

**Mifflin-St Jeor formülü** kullanılarak günlük:

* Kalori ihtiyacı
* Protein
* Karbonhidrat
* Yağ

otomatik olarak hesaplanır.

---

### 🔔 Akıllı Hatırlatıcı

**WorkManager** kullanılarak antrenman yapılmayan günlerde kullanıcıya **bildirim gönderilir**.

---

# 🛠 Kullanılan Teknolojiler

**Dil**

* Kotlin

**UI**

* Jetpack Compose

**Veritabanı**

* Room Database

**Arka Plan İşlemleri**

* WorkManager

**Görsel İşleme**

* Coil

**Navigasyon**

* Jetpack Navigation

**Mimari**

* MVVM Pattern
* Repository Pattern

---

# 🚀 Teknik Detaylar

### Veri Bütünlüğü

**ExerciseLibrary sistemi** sayesinde egzersiz isimleri standartlaştırılır.

Örneğin:

Bench Press
Benchpress

gibi veri karışıklıkları oluşmaz.

---

### Scoped Storage

Android'in güncel **Scoped Storage** politikalarına uygun olarak seçilen görseller uygulamanın özel dizinine kopyalanır.

Bu sayede:

* Galeriden silinse bile
* uygulamadaki gelişim fotoğrafları korunur.

---

### State Management

Jetpack Compose içinde:

* `mutableStateListOf`
* `collectAsState`

kullanılarak **gerçek zamanlı UI güncellemeleri** sağlanır.

---

# 📥 Kurulum

### 1️⃣ Depoyu klonlayın

```bash
git clone https://github.com/SabanCE/GymTracking.git
```

### 2️⃣ Android Studio'da açın

Projeyi **Android Studio** ile açın.

---

### 3️⃣ Gradle bağımlılıklarını yükleyin

Proje açıldığında **Gradle Sync** tamamlanmasını bekleyin.

---

### 4️⃣ Uygulamayı çalıştırın

Bir:

* Android Emulator
  veya
* Fiziksel Android cihaz

üzerinde çalıştırabilirsiniz.

---

# 👨‍💻 Geliştirici

**Şaban Can Evran**

GitHub:
https://github.com/SabanCE
