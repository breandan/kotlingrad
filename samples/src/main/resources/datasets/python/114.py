class EmailLexer(RegexLexer):
    tokens = {
        'root': [
            (r'\s*<[^>]*>', Keyword),
            (r'^([^:]*)(: )', bygroups(Name.Attribute, Operator)),
            (r'.*\n', Text)
        ]
    }
