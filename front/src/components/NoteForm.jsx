import React, {useState} from "react";

const NoteForm = ({postNote, editedNote, updateNote}) => {

    const [newNoteTitle, setNewNoteTitle] = useState(editedNote?.content?.title || '');
    const [newNoteContent, setNewNoteContent] = useState(editedNote?.content?.body || '');

    const submitNote = (event) => {
        event.preventDefault();
        if (editedNote) {
            updateNote(newNoteTitle, newNoteContent)
        } else {
            postNote(newNoteTitle, newNoteContent);
        }
        setNewNoteTitle("");
        setNewNoteContent("");
    }

    const onChangeNoteTitle = event => {
        setNewNoteTitle(event.currentTarget.value);
    }

    const onChangeNoteContent = event => {
        setNewNoteContent(event.currentTarget.value);
    }

    React.useEffect(() => {
        if(editedNote){
            setNewNoteTitle(editedNote.content.title);
            setNewNoteContent(editedNote.content.body);
        }
    }, [editedNote])

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
