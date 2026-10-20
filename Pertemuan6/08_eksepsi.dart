void main() {
  try {
    int a = 5 ~/ 0;
    print(a);
  } on UnsupportedError {
    print("Operasi tidak didukung");
  }

  try {
    int a = 5 ~/ 0;
    print(a);
  } catch (e) {
    print(e);
  }
}
