var paths = [
    // header
    ".header",
    "body > div.wrapper > div.site > div.pagehead.repohead.instapaper_ignore.readability-menu > div > h1",

    // watch/star/fork buttons
    "body > div.wrapper > div.site > div.pagehead.repohead.instapaper_ignore.readability-menu > div > ul",

    // edit and new issue buttons
    "#partial-discussion-header > div.gh-header-show > div > button",
    "#partial-discussion-header > div.gh-header-show > div > a",

    // sidebar
    "#discussion_bucket > div.discussion-sidebar",

    // right navbar
    "body > div.wrapper > div.site > div.container > div.repository-with-sidebar.repo-container.new-discussion-timeline > div.repository-sidebar.clearfix > nav",

    // select parts of the footer
    "body > div.container > div > ul:nth-child(3) > li:nth-child(2) > a",
    "body > div.container > div > ul:nth-child(3) > li:nth-child(3) > a",
    "body > div.container > div > ul:nth-child(3) > li:nth-child(4) > a",
    "body > div.container > div > ul:nth-child(3) > li:nth-child(5) > a",
    "body > div.container > div > ul.site-footer-links.right > li:nth-child(1) > a",
    "body > div.container > div > ul.site-footer-links.right > li:nth-child(2) > a",
    "body > div.container > div > ul.site-footer-links.right > li:nth-child(3) > a",
    "body > div.container > div > ul.site-footer-links.right > li:nth-child(4) > a",
    "body > div.container > div > ul.site-footer-links.right > li:nth-child(5) > a",
    "body > div.container > div > ul.site-footer-links.right > li:nth-child(6) > a",
    "body > div.container > div > a > span",
];

paths.forEach(function (path) {
    $(path).hide();
});
