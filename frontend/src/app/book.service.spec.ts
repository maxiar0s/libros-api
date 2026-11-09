import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { BookService } from './book.service';

describe('BookService', () => {
  let service: BookService;
  let http: HttpTestingController;
  const data = { title: 'El Hobbit', author: 'J. R. R. Tolkien', publicationYear: 1937, genre: 'Fantasía' };
  const book = { id: 1, ...data };

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(BookService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('lists books', () => {
    service.list().subscribe(result => expect(result).toEqual([book]));
    const request = http.expectOne('/api/books');
    expect(request.request.method).toBe('GET');
    request.flush([book]);
  });
  it('retrieves a book by id', () => {
    service.get(1).subscribe(result => expect(result).toEqual(book));
    const request = http.expectOne('/api/books/1');
    expect(request.request.method).toBe('GET');
    request.flush(book);
  });
  it('creates a book', () => {
    service.create(data).subscribe(result => expect(result).toEqual(book));
    const request = http.expectOne('/api/books');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(data);
    request.flush(book);
  });
  it('updates a book', () => {
    service.update(1, data).subscribe(result => expect(result).toEqual(book));
    const request = http.expectOne('/api/books/1');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(data);
    request.flush(book);
  });
  it('deletes a book by id', () => {
    service.delete(1).subscribe();
    const request = http.expectOne('/api/books/1');
    expect(request.request.method).toBe('DELETE');
    request.flush(null, { status: 204, statusText: 'No Content' });
  });
});
