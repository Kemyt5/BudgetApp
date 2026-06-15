import React, { useState, useEffect } from 'react';
import axios from 'axios';

function App() {
  const [transactions, setTransactions] = useState([]);
  const [formData, setFormData] = useState({ amount: '', type: 'INCOME', tags: '', notes: '' });

  const auth = { username: 'admin', password: 'admin' };

  const fetchTransactions = () => {
    axios.get('http://localhost:8080/api/transactions', { auth })
        .then(res => setTransactions(res.data))
        .catch(err => console.error(err));
  };

  useEffect(() => { fetchTransactions(); }, []);

  const handleSubmit = (e) => {
    e.preventDefault();
    axios.post('http://localhost:8080/api/transactions', formData, { auth })
        .then(() => {
          fetchTransactions();
          setFormData({ amount: '', type: 'INCOME', tags: '', notes: '' });
        })
        .catch(err => alert("Błąd!"));
  };

  const deleteTransaction = (id) => {
    axios.delete(`http://localhost:8080/api/transactions/${id}`, { auth })
        .then(() => fetchTransactions());
  };

  return (
      <div style={{ padding: '20px' }}>
        <h1>Menedżer Finansów</h1>

        <form onSubmit={handleSubmit} style={{ marginBottom: '20px', border: '1px solid #ddd', padding: '10px' }}>
          <input type="number" placeholder="Kwota" value={formData.amount} onChange={e => setFormData({...formData, amount: e.target.value})} required />
          <select value={formData.type} onChange={e => setFormData({...formData, type: e.target.value})}>
            <option value="INCOME">Przychód</option>
            <option value="EXPENSE">Wydatek</option>
          </select>
          <input type="text" placeholder="Tagi" value={formData.tags} onChange={e => setFormData({...formData, tags: e.target.value})} />
          <input type="text" placeholder="Notatki" value={formData.notes} onChange={e => setFormData({...formData, notes: e.target.value})} />
          <button type="submit">Dodaj</button>
        </form>

        <table border="1" style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
          <tr><th>ID</th><th>Kwota</th><th>Typ</th><th>Tagi</th><th>Notatki</th><th>Akcje</th></tr>
          </thead>
          <tbody>
          {transactions.map(t => (
              <tr key={t.id}>
                <td>{t.id}</td>
                <td>{t.amount} zł</td>
                <td>{t.type}</td>
                <td>{t.tags}</td>
                <td>{t.notes}</td>
                <td><button onClick={() => deleteTransaction(t.id)}>Usuń</button></td>
              </tr>
          ))}
          </tbody>
        </table>
      </div>
  );
}

export default App;