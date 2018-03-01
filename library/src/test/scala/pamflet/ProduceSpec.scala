package pamflet

import java.io.File
import java.nio.file.{ Files, Path }
import java.util.Collections
import java.util.function.Consumer

import utest._

object ProduceSpec extends TestSuite {
  val tests = Tests {
    'localPath - withTempDir { tempDir =>
      produce(new File("src/test/pf"), tempDir.toFile)
      val obtained = readFileString(tempDir resolve "test.html")
      val expected = "https://github.com/foundweekends/pamflet/edit/master/src/test/pf/test.md"
      assert(obtained contains expected)
    }
  }

  private def produce(srcDir: File, outDir: File) =
    Produce(FileStorage(srcDir, Nil).globalized, outDir)

  private def withTempDir[A](thunk: Path => A): A = {
    val tempDir = Files createTempDirectory "pamflet-test-"
    try thunk(tempDir)
    finally deleteDir(tempDir)
  }

  private def deleteDir(p: Path): Unit =
    Files
      .walk(p)
      .sorted(Collections.reverseOrder())
      .forEach(new Consumer[Path] { def accept(p: Path): Unit = Files delete p })

  private def readFileString(p: Path): String = new String(Files readAllBytes p)
}
