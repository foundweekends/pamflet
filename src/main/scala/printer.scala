package pamflet
import com.tristanhunt.knockoff.DefaultDiscounter.toXHTML

class Printer(contents: Seq[Page]) {
  def toc(current: Page) =
    <ol> { contents.map {
      case `current` => <li>{ current.name }</li>
      case page => <li>
        <a href={ page.name }>{ page.name }</a> 
      </li>
    } } </ol>

  def print(page: Page) =
    <html>
      <head><title>{ page.name }</title></head>
      <body>
        { toXHTML(page.blocks) ++ toc(page) }
      </body>
    </html>

  def printNamed(name: String) =
    contents.find { _.name == name }.map(print)
}      
    
