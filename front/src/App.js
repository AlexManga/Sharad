// /!\ l'ordre d'import est important. Importer bootstrap en dernier fait que bootstrap override tous les autres css
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'
import React, {Component} from 'react';
import './App.css';
import Note from "./components/Note";
import NoteForm from "./components/NoteForm";


class App extends Component {

    state = {
        notes: []
    };

    componentDidMount() {
        this.fetchAllNotes();
    }

    postNote = (title, body) => {
        const request = {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                "title": title,
                "body": body,
            })
        }

        fetch('/note', request)
            .then(res => res.json())
            .then((data) => {
                console.log(data);
                this.fetchAllNotes();
            })
            .catch(console.log)
    }


    fetchAllNotes() {
        const request = {
            method: 'GET',
            headers: {'Content-Type': 'application/json'}
        }
        fetch('/note', request)
            .then(res => res.json())
            .then((data) => {
                this.setState({notes: data});
            })
            .catch(console.log)
    }

    deleteNote(id) {
        const request = {
            method: 'DELETE'
        }
        fetch('/note/' + id, request)
            .then(() => {
                this.fetchAllNotes();
            })
            .catch(console.log)
    }

    render() {
        return (
            <main className="App">
                <div className="container">
                    <div className="row">
                        {this.state.notes.map((note) => (
                            <Note key={note.noteId.id}
                                  note={note}
                                  deleteNote={() => this.deleteNote(note.noteId.id)}/>
                        ))}
                    </div>
                    <hr/>
                    <NoteForm postNote={this.postNote}/>
                </div>
            </main>
        );
    }
}

export default App;
