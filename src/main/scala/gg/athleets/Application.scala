package gg.athleets

object Application {
  def main(args: Array[String]): Unit = {
    val service = new Service
    service.start()
  }
}