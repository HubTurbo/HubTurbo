var paths = [
    // header
    "body > header > div > div",

    // footer
    "body > footer > div > ul > li > form > button",
];

paths.forEach(function (path) {
    $(path).hide();
});
