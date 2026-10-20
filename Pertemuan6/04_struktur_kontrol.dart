void main() {
  int angka = 4;
  if (angka > 5) {
    print("Angka lebih besar dari 5");
  } else {
    print("Angka tidak lebih dari 5");
  }

  for (int i = 0; i < 5; i++) {
    print("Iterasi ke-$i");
  }

  while (angka > 0) {
    print("Nilai angka: $angka");
    angka--;
  }
}
