import React, { useState } from "react";

const NoteForm = ({postNote}) => {

    const [newNoteTitle, setNewNoteTitle] = useState("");
    const [newNoteContent, setNewNoteContent] = useState("");

    const submitNote = (event) => {
        event.preventDefault();
        postNote(newNoteTitle, newNoteContent);
        setNewNoteTitle("");
        setNewNoteContent("");
    }

    const onChangeNoteTitle = event => {
        setNewNoteTitle(event.currentTarget.value);
    }

    const onChangeNoteContent = event => {
        setNewNoteContent(event.currentTarget.value);
    }

    return (
        <div className="row justify-content-center">
            <form onSubmit={submitNote}>
                <legend>Création d'une note :</legend>
                <div className="mb-3">
                    <label htmlFor="inputTitle" className="form-label">Titre</label>
                    <input value={newNoteTitle}
                           onChange={onChangeNoteTitle}
                           type="text"
                           className="form-control"
                           id="inputTitle"/>
                </div>
                <div className="mb-3">
                    <label htmlFor="inputContent" className="form-label">Contenu</label>
                    <textarea
                        value={newNoteContent}
                        onChange={onChangeNoteContent}
                        className="form-control"
                        id="inputContent"/>
                </div>
                <button type="submit" className="btn btn-primary">Submit
                </button>
            </form>
        </div>);

}

export default NoteForm;
