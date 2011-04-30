package pamflet
import com.tristanhunt.knockoff.DefaultDiscounter.toXHTML

object Printer {
  def webify(name: String) =
    java.net.URLEncoder.encode(name, "utf-8") + ".html"
}
class Printer(contents: Contents) {
  def toc(current: Page) =
    <ol> { contents.pages.map {
      case `current` => <li>{ current.name }</li>
      case page => <li>
        <a href={ Printer.webify(page.name) }>{ page.name }</a> 
      </li>
    } } </ol>

  def print(page: Page) =
    <html>
      <head>
        <title>{ "%s: %s".format(contents.title, page.name) }</title>
        <link rel="stylesheet" href="css/blueprint/screen.css" type="text/css" media="screen, projection"/>
        <link rel="stylesheet" href="css/blueprint/print.css" type="text/css" media="print"/> 
        <!--[if lt IE 8]>
          <link rel="stylesheet" href="css/blueprint/ie.css" type="text/css" media="screen, projection"/>
        <![endif]-->
        <link rel="stylesheet" href="css/pamflet.css" type="text/css" media="screen, projection"/> 
      </head>
      <body>
        <div class="container">
          { toXHTML(page.blocks) ++ toc(page) }
        </div>
      </body>
    </html>

  def printNamed(name: String) =
    contents.pages.find { page =>
      Printer.webify(page.name) == name
    }.map(print)
}      
    
