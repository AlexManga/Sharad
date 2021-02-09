// /!\ l'ordre d'import est important. Importer bootstrap en dernier fait que bootstrap override tous les autres css
import 'bootstrap/dist/css/bootstrap.min.css'
import React, {Component} from 'react';
import './App.css';


class App extends Component {

    state = { inputTextBoxDisplayed: false };
    noteInputRef;

    constructor() {
        super();
        this.noteInputRef = React.createRef();
    }

    displayTextBoxInput = () => {
        this.setState({ inputTextBoxDisplayed: true });
    }

    postNote = () => {
        const val = this.noteInputRef.current.value;
        const request = {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' },
            // body: JSON.stringify({ content: `${val}` })
        }
        fetch('/note', request)
        .then(res => res.json())
        .then((data) => {
            console.log(data);
        })
        .catch(console.log)
    }

    render() {
        return (
            <main className="App">
                <div className="container">
                    <div className="px-5">
                        <button className="btn btn-outline-primary" onClick={ this.displayTextBoxInput } hidden={ this.state.inputTextBoxDisplayed } >+</button>
                        <div className="form-group mb-0" hidden={ !this.state.inputTextBoxDisplayed }>
                            <textarea id="note-input" className="form-control" ref={ this.noteInputRef }></textarea>
                            <div className="row mx-0">
                                <button className="btn btn-outline-primary offset-10 col-2" onClick={ this.postNote }>
                                    Save
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        );
    }
}

export default App;
