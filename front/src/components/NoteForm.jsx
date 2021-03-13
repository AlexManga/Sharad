import React, {Component} from "react";

class NoteForm extends Component {

    state = {
        newNoteTitle: "",
        newNoteContent: ""
    };

    submitNote = event => {
        event.preventDefault();
        this.props.postNote(this.state.newNoteTitle, this.state.newNoteContent);
        this.setState({
            newNoteTitle: "",
            newNoteContent: ""
        })
    }

    onChangeNoteTitle = event => {
        this.setState({newNoteTitle: event.currentTarget.value})
    }

    onChangeNoteContent = event => {
        this.setState({newNoteContent: event.currentTarget.value})
    }

    render() {
        return (
            <div className="row justify-content-center">
                <form onSubmit={this.submitNote}>
                    <legend>Création d'une note :</legend>
                    <div className="mb-3">
                        <label htmlFor="inputTitle" className="form-label">Titre</label>
                        <input value={this.state.newNoteTitle}
                               onChange={this.onChangeNoteTitle}
                               type="text"
                               className="form-control"
                               id="inputTitle"/>
                    </div>
                    <div className="mb-3">
                        <label htmlFor="inputContent" className="form-label">Contenu</label>
                        <textarea
                            value={this.state.newNoteContent}
                            onChange={this.onChangeNoteContent}
                            className="form-control"
                            id="inputContent"/>
                    </div>
                    <button type="submit" className="btn btn-primary">Submit
                    </button>
                </form>
            </div>)
    }

}

export default NoteForm;
