@app.context_processor
def render_helpers():
    return dict(render_headers=render_headers, render_patch=render_patch)
