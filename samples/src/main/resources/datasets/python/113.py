def render_patch(data):
    lexer = lexers.get_lexer_by_name('diff')
    formatter = CodeHtmlFormatter(encoding='utf-8')
    return Markup(highlight(data, lexer, formatter).decode('utf-8'))
