import java.io.File;
import javax.swing.*;
import java.awt.*;

public class Main {

    // Penceredeki sonuçları yazacağımız alan.
    // static: tüm metotlar bu alana ulaşabilsin diye sınıfın en üstünde tanımladık.
    static JTextArea kayitAlani = new JTextArea();

    public static void main(String[] args) {
        // ---------- 1) PENCEREYİ OLUŞTUR ----------
        JFrame pencere = new JFrame("Dosya Düzenleyici");
        pencere.setSize(600, 400);
        pencere.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // X'e basınca program kapansın
        pencere.setLayout(new BorderLayout()); // pencereyi üst/orta/alt bölgelere ayırır

        // ---------- 2) ÜSTE BUTON ----------
        JButton buton = new JButton("Klasör Seç ve Düzenle");
        pencere.add(buton, BorderLayout.NORTH);

        // ---------- 3) ORTAYA SONUÇ ALANI ----------
        kayitAlani.setEditable(false); // kullanıcı içine yazamasın
        pencere.add(new JScrollPane(kayitAlani), BorderLayout.CENTER); // kaydırma çubuğu ekler

        // ---------- 4) BUTONA BASILINCA NE OLACAK ----------
        buton.addActionListener(e -> {
            // Klasör seçme penceresi aç
            JFileChooser secici = new JFileChooser();
            secici.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // sadece klasör seçilebilsin
            int sonuc = secici.showOpenDialog(pencere);

            if (sonuc == JFileChooser.APPROVE_OPTION) { // kullanıcı "Aç"a bastıysa
                File klasor = secici.getSelectedFile();

                // Güvenlik: taşımadan önce emin misin diye sor
                int cevap = JOptionPane.showConfirmDialog(pencere,
                        klasor.getPath() + "\nklasöründeki dosyalar kategorilere taşınacak. Emin misin?",
                        "Onay", JOptionPane.YES_NO_OPTION);

                if (cevap == JOptionPane.YES_OPTION) {
                    kayitAlani.setText(""); // önceki sonuçları temizle
                    duzenle(klasor);
                }
            }
        });

        pencere.setVisible(true); // pencereyi göster
    }

    // Sonuç alanına bir satır ekler (System.out.println'in pencere versiyonu)
    static void yaz(String mesaj) {
        kayitAlani.append(mesaj + "\n");
    }

    // ================= ASIL İŞİ YAPAN METOT =================
    // Senin yazdığın kod, artık buraya taşındı
    static void duzenle(File klasor) {
        File[] icindekiler = klasor.listFiles();
        if (icindekiler == null) {
            yaz("Klasör bulunamadı :(");
            return; // metottan çık
        }

        int tasinanSayisi = 0;

        for (File f : icindekiler) {
            if (f.isDirectory()) {
                continue; // klasörse atla, sıradaki dosyaya geç
            }

            String isim = f.getName();
            int nokta = isim.lastIndexOf(".");

            // Önce güvenli varsayılan değerler, sonra nokta varsa kes
            String ad = isim;
            String uzanti = "";
            if (nokta != -1) {
                ad = isim.substring(0, nokta);
                uzanti = isim.substring(nokta + 1).toLowerCase();
            }

            String kategori = kategoriBul(uzanti);

            // Kategori klasörü yoksa oluştur
            File hedefKlasor = new File(klasor, kategori);
            if (!hedefKlasor.exists()) {
                hedefKlasor.mkdir();
                yaz("📁 " + kategori + " klasörü oluşturuldu");
            }

            // Aynı isimde dosya varsa (1), (2)... ekle
            File hedefDosya = new File(hedefKlasor, isim);
            int sayac = 1;
            while (hedefDosya.exists()) {
                String yeniIsim;
                if (uzanti.equals("")) {
                    yeniIsim = ad + " (" + sayac + ")";           // README (1)
                } else {
                    yeniIsim = ad + " (" + sayac + ")." + uzanti; // ders (1).pdf
                }
                hedefDosya = new File(hedefKlasor, yeniIsim);
                sayac++;
            }

            // Taşı
            if (f.renameTo(hedefDosya)) {
                yaz("✓ " + isim + "  →  " + kategori + "/" + hedefDosya.getName());
                tasinanSayisi++;
            } else {
                yaz("✗ " + isim + " taşınamadı");
            }
        }

        yaz("");
        yaz("Bitti! " + tasinanSayisi + " dosya taşındı.");
    }

    // Uzantıya göre kategori adını döndürür
    static String kategoriBul(String uzanti) {
        switch (uzanti) {
            case "pdf", "docx", "txt":
                return "belgeler";
            case "jpg", "png":
                return "resimler";
            case "mp4", "mov":
                return "videolar";
            case "zip", "rar", "7z":
                return "sıkıştırılmışlar";
            case "exe", "msi":
                return "programlar";
            default:
                return "diğer";
        }
    }
}