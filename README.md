🏋️ GymTracking - Modern Fitness & Progressive Overload Assistant
GymTracking, sporcuların gelişimlerini bilimsel yöntemlerle (Progressive Overload) takip etmelerini sağlayan, Jetpack Compose ile geliştirilmiş modern bir Android uygulamasıdır. Kullanıcıların antrenman programlarını yönetmelerine, rekorlarını (PR) takip etmelerine ve beslenme hedeflerini hesaplamalarına olanak tanır.
📸 Ekran Görüntüleri
Ana Ekran
Program Yönetimi
Antrenman Takibi
Gelişim & Fotoğraf
<img src="https://i.imgur.com/vHqB37f.png" width="200">
<img src="https://i.imgur.com/kS5x87e.png" width="200">
<img src="https://i.imgur.com/lO7S9M1.png" width="200">
<img src="https://i.imgur.com/qA6W2S2.png" width="200">
(Not: Yukarıdaki linkler temsilidir, kendi ekran görüntülerini screenshots/ klasörüne ekleyip yolları güncelleyebilirsin.)
✨ Öne Çıkan Özellikler
•
📈 Progressive Overload Takibi: Antrenman sırasında her egzersiz için bir önceki haftanın set, tekrar ve ağırlık verilerini (PR) otomatik olarak karşınıza getirir.
•
📅 Akıllı Gün Yönetimi: Programların bittikçe başa döndüğü ancak "Toplam Gün Sayacı"nın takvim gibi ilerlediği dinamik bir sistem.
•
📂 Egzersiz Kütüphanesi: Veri tutarlılığını sağlamak için kategorize edilmiş geniş egzersiz kütüphanesi ve özel egzersiz ekleme seçeneği.
•
📸 Fotoğraf Günlüğü: Gelişim fotoğraflarını uygulama klasörüne güvenli bir şekilde kopyalayarak (Internal Storage) tarih bazlı saklama.
•
🍎 Beslenme Asistanı: Mifflin-St Jeor formülünü kullanarak günlük kalori ve makro (Protein, Karb, Yağ) ihtiyacı hesaplama.
•
🔔 Akıllı Hatırlatıcı: WorkManager kullanarak antrenmanını tamamlamayan kullanıcılara günlük bildirim gönderme.
🛠 Kullanılan Teknolojiler
•
Dil: Kotlin
•
UI: Jetpack Compose (Modern deklaratif arayüz)
•
Veritabanı: Room Database (Yerel veri saklama ve Flow entegrasyonu)
•
Arka Plan İşlemleri: WorkManager (Zamanlanmış bildirimler)
•
Görsel Yükleme: Coil (Hızlı ve optimize görsel işleme)
•
Navigasyon: Jetpack Navigation
•
Mimari: MVVM Pattern & Repository Pattern temelleri
🚀 Teknik Detaylar
•
Veri Bütünlüğü: ExerciseLibrary sistemi sayesinde "Bench Press" verisi "Benchpress" ile karışmaz, gelişim takibi her zaman doğru yapılır.
•
Scoped Storage: Android'in güncel dosya politikalarına uygun olarak seçilen görseller uygulamanın özel dizinine kopyalanır, böylece galeri silinse dahi veriler korunur.
•
State Management: mutableStateListOf ve collectAsState kullanılarak gerçek zamanlı UI güncellemeleri optimize edilmiştir.
📥 Kurulum
1.
Bu depoyu klonlayın:
Shell Script
git clone https://github.com/SabanCE/GymTracking.git
2.
Android Studio'da projeyi açın.
3.
Gerekli bağımlılıkların yüklenmesini bekleyin (Gradle Sync).
4.
Bir emülatör veya fiziksel cihazda çalıştırın.
👨‍💻 Geliştirici: Şaban Can Evran
