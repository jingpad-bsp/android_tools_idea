class StringCategory {
  static String getMd5(String[] s) {
    return "MD5Value";//suppose to be some code that generate md5
  }
}

use(StringCategory) {
  println new String[2].md5<caret>
}
