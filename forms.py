from flask import Flask, make_response

app = Flask(__name__)

@app.route("/<id>")
def form(id):
    html = """
        <div style="position:fixed;top:0px;left:0px;right:0px;bottom:0px;"><div data-fillout-id="{{id}}" data-fillout-embed-type="fullscreen" style="width:100%;height:100%;" data-fillout-inherit-parameters ></div><script src="https://server.fillout.com/embed/v1/"></script></div>
    """
    if id == "sticker-designer-application":
        return make_response(html.replace("{{id}}", "1XdSSYvu3jus"))
    return make_response(html.replace("{{id}}", id))