def render_headers(data):
    lexer = EmailLexer()
    formatter = HtmlFormatter(encoding='utf-8')
    return Markup(highlight(data, lexer, formatter).decode('utf-8'))
