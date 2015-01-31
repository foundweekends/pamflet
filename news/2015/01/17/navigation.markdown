Navigating up
=============

We aren't overly concerned with the front page produced by Pamflet News, since destination blogs by amateurs are practically dead. But our news should be easily browsable, and there are some cases of browsing that aren't well handled by the initial alpha version.

When readers arrive at a news item, either from a web search or social media, they're presented with a link at the end of the item to go to the next older story. This fits with Pamflet's model of simple linear navigation: news pages are presented newest to oldest, so pages to the right are older. That works well enough.

But visitors should also be able to find newer stories, and perhaps a word about the project that the news relates to. To support that, I've made Pamflet News a little more like the original Pamflet: the front page must also have a backing markdown document. We'll use this for overview content, as well as the title of the publication -- e.g., "Pamflet News". This way, every page has body content that can be consistently followed by a list of news stories (the navigation).

With change we are more or less feature-complete, but not a bit scalable: we'll need pagination to support more than a few dozen news items. It's not hugely complicated, but I'm going to put off doing it until I'm certain that I'll write that much.
