import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

export interface BookInput {
  title: string;
  author: string;
  publicationYear: number;
  genre: string;
}

export interface Book extends BookInput {
  id: number;
}

@Injectable({ providedIn: 'root' })
export class BookService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/books';

  list() { return this.http.get<Book[]>(this.url); }
  get(id: number) { return this.http.get<Book>(`${this.url}/${id}`); }
  create(book: BookInput) { return this.http.post<Book>(this.url, book); }
  update(id: number, book: BookInput) { return this.http.put<Book>(`${this.url}/${id}`, book); }
  delete(id: number) { return this.http.delete<void>(`${this.url}/${id}`); }
}
